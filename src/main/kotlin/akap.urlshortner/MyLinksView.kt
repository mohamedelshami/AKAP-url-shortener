package akap.urlshortner

import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import react.*
import react.dom.*

data class LinkDetails(val blockNumber: Int, val sender: String, val nodeId: String, val label: String, val url: String)

interface MyLinksViewState : RState {
    var myLinks: List<LinkDetails>?
    var loaded: Boolean?
    var selectedAccount: String?
}

class MyLinksView : RComponent<RProps, MyLinksViewState>() {

    override fun componentDidMount() {
        /* This is async block - but it should be safe to initialise here as componentDidMount blocks JS single thread */
        coroutineAppScope.launch {
            Web3.enable().asDeferred().await()
            val accounts = Web3.getAccounts().asDeferred().await()
            setState {
                selectedAccount = accounts[0]
            }
            URLShortenerContract.create()
            AKAPContract.create()
            getRecentLinks()
        }
    }

    override fun RBuilder.render() {
        if (state.myLinks?.isNotEmpty() == true) {
            table("table table-dark") {
                th(classes = "thead-dark") {
                    +"Short Label"
                }
                th(classes = "thead-dark") {
                    +"Link"
                }
                th(classes = "thead-dark") {
                    +"Node ID"
                }
                state.myLinks?.forEach {
                    val labelTxt = "$redirectBase#" + it.label
                    val url = it.url
                    val nodeId = it.nodeId
                    tr {
                        td {
                            +labelTxt
                        }
                        td {
                            a(href = "$url", classes = "alert-link") { +url }
                        }
                        td {
                            a(href = "https://akap.me/browser/$nodeId") { +(nodeId.substring(0, 8) + "..") }
                        }
                    }
                }
            }
        } else if (Web3.isSupported() && state.loaded != true) {
            div(classes = "spinner-grow text-primary") {}
            p { +"Retrieving recent links.." }
        } else if (Web3.isSupported() && state.loaded == true) {
            p { +"No links found on your account.." }
        }
    }

    private fun getRecentLinks() {
        coroutineAppScope.launch {
            val transfers: Array<dynamic> = AKAPContract.getPastEvents("Transfer" ).asDeferred().await() as Array<dynamic>
            val events: Array<dynamic> = AKAPContract.getPastEvents("Claim" ).asDeferred().await() as Array<dynamic>
            val txs = transfers.filter {  it.returnValues.to == state.selectedAccount }.map { it.transactionHash }.toSet()
            val parentNodeId = URLShortenerContract.parentNodeId().asDeferred().await()

            val res: List<LinkDetails> = events.filter { event -> txs.contains( event.transactionHash ) && parentNodeId == event.returnValues.parentId }.mapNotNull { event ->
                val blockNumber = event.blockNumber as Int
                val sender = event.returnValues.sender as String
                val nodeId = Web3js.utils.toHex(event.returnValues.nodeId) as String
                val nodeLabel = hexToString(event.returnValues.label)
                val url = getNodeBody(event.returnValues.nodeId)

                if (nodeLabel != null && url != null)
                    LinkDetails(blockNumber, sender, nodeId, nodeLabel, url)
                else
                    null
            }.sortedByDescending { it.blockNumber }

            setState {
                myLinks = res
                loaded = true
            }
        }
    }
}

package akap.urlshortner

import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import react.*
import react.dom.*

interface MyLinksViewState : RState {
    var myLinks: List<Map<String, Any>>
    var selectedAccount: String
}

class MyLinksView : RComponent<RProps, MyLinksViewState>() {

    override fun componentDidMount() {
        /* This is async block - but it should be safe to initialise here as componentDidMount blocks JS single thread */
        coroutineAppScope.launch {
            val accounts = Web3.enable().asDeferred().await()
            setState {
                selectedAccount = accounts[0]
            }
            AKAPContract.create()
            getRecentLinks()
        }
    }

    override fun RBuilder.render() {
        if (!state.myLinks.isNullOrEmpty()) {
            table("table table-dark") {
                th(classes = "thead-dark") {
                    +"Short Label"
                }
                th(classes = "thead-dark") {
                    +"Link"
                }
                state.myLinks.forEach {
                    val labelTxt = "$redirectBase#" + it["label"]
                    val lnk: String = "" + it["url"]
                    tr {
                        td {
                            +labelTxt
                        }
                        td {
                            a(href = "$lnk", classes = "alert-link") { +lnk }
                        }
                    }
                }
            }
        } else if (Web3.isSupported()) {
            div(classes = "spinner-grow text-primary") {}
            p { +"Retrieving recent links.." }
        }
    }

    private fun getRecentLinks() {
        coroutineAppScope.launch {
            val events: Array<dynamic> = AKAPContract.getPastEvents("Claim", state.selectedAccount).asDeferred().await() as Array<dynamic>
            val res: List<Map<String, Any>> = events.filter { event -> event.returnValues.claimCase == 1 }.map { event ->
                val url = getNodeBody(event.returnValues.nodeId)
                val nodeLabel = hexToString(event.returnValues.label)
                mapOf("blockNumber" to event.blockNumber, "sender" to event.returnValues.sender, "nodeId" to event.returnValues.nodeId, "label" to nodeLabel, "url" to url)
            }.sortedByDescending { event -> event["blockNumber"] as Int }

            setState {
                myLinks = res
            }
        }
    }
}

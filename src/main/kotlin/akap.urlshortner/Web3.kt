package akap.urlshortner

import kotlin.browser.window
import kotlin.js.Promise

/* TODO: Figure out interface wiring using JsModule annotation */
val truffleContract: dynamic = js("TruffleContract")
val Web3js = js("Web3")

object Web3 {
    private var web3Obj: dynamic = null
    private var provider: dynamic = null

    fun get(): dynamic {
        return web3Obj
    }

    init {
        console.log("Web3 JS API Version " + Web3js.version)
        if (window.asDynamic().ethereum != undefined) {
            provider = window.asDynamic().ethereum
            if (provider.isMetaMask)
                provider.autoRefreshOnNetworkChange = false // MetaMask API will remove auto-refresh support
            this.web3Obj = js("new Web3(window.ethereum)")
        } else if (Web3js.givenProvider != undefined) {
            console.log("Try use given provider.")
            provider = Web3js.givenProvider
            this.web3Obj = js("new Web3(Web3.givenProvider)")
        } else
            console.warn("No Web3 provider was detected.")
    }

    fun isSupported(): Boolean {
        return web3Obj != null && provider != null
    }

    fun getAccounts(): Promise<Array<String>> {
        return web3Obj.eth.getAccounts()
    }

    fun getNetworkId(): Promise<Int> {
        return web3Obj.eth.net.getId()
    }

    fun enable(): Promise<Array<String>> {
        return provider.enable()
    }

}

fun fetchContract(cf: String): Promise<Promise<Any?>> = window.fetch("contracts/$cf").then {
    it.json()
}

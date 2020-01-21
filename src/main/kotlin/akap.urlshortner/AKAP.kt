package akap.urlshortner

import kotlinext.js.JsObject
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.Promise

fun randomAlphaNumeric(n: Int): String {
    val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (0..n).map {
        chars.random()
    }.joinToString("")
}

fun stringToBytes(str: String): ByteArray {
    val bytes = ByteArray(str.length)
    str.forEachIndexed { i, c ->
        bytes.set(i, c.toByte())
    }
    return bytes
}

// TODO: Make claim and setNodeNody atomic composing the calls into a batch request
suspend fun createShortLink(longURL: String, fromAccount: String, checkOwner: Boolean = false): String {
    val linkLabel = randomAlphaNumeric(7)
    val labelBytes = stringToBytes(linkLabel)
    val urlBytes = stringToBytes(longURL)

    val hash = AKAPContract.hashOf(0, labelBytes).asDeferred().await()

    if (checkOwner && fromAccount == AKAPContract.ownerOf(hash).asDeferred().await()) {
        console.warn("Account ${fromAccount} is already an owner.")
    } else {
        val response = URLShortenerContract.claimAndSetNodeBody(labelBytes, urlBytes, fromAccount).then {
            console.log("Successfully created node $hash")
        }.asDeferred().await()
        console.log("Response: " + response)
    }

    console.log("Link $linkLabel created for $longURL")

    return linkLabel
}

suspend fun getURLFromLabel(label: String): String? {
    val labelBytes = stringToBytes(label)
    val parentNodeId = URLShortenerContract.parentNodeId().asDeferred().await()
    val nodeHash = AKAPContract.hashOf(parentNodeId, labelBytes).asDeferred().await()
    AKAPContract.ownerOf(nodeHash).then {
        console.log("Retrieving a link for $label - created by $it")
    }
    return getNodeBody(nodeHash)
}

suspend fun getNodeBody(nodeId: JsObject): String? {
    val data = AKAPContract.nodeBody(nodeId).asDeferred().await()
    return hexToString(data)
}

fun hexToString(hex: String): String? {
    if (hex == undefined) return null;
    return Web3js.utils.hexToString(hex)
}

object AKAPContract {
    private var tfc: dynamic = null

    fun get(): AKAPContract {
        return tfc
    }

    init {
        coroutineAppScope.launch {
            create()
        }
    }

    suspend fun create() {
        val json = fetchContract("IAKAP.json").asDeferred().await()
        tfc = truffleContract(json)
        tfc.setProvider(Web3.get().currentProvider)
        console.log("Created AKAP contract " + tfc)
    }

    fun hashOf(parentId: Int, label: ByteArray): Promise<dynamic> {
        val response = tfc.deployed().then { instance ->
            instance.contract.methods.hashOf(parentId, label).call()
        }
        return response as Promise<Int>
    }

    fun claim(parentId: Int, label: ByteArray, fromAccount: String): Promise<Json> {
        val params = js("{from: '' }")
        params.from = fromAccount
        return tfc.deployed().then { instance ->
            instance.contract.methods.claim(parentId, label).send(params)
        } as Promise<Json>
    }

    fun setNodeBody(nodeId: JsObject, data: ByteArray, fromAccount: String): Promise<dynamic> {
        val params = js("{from: '' }")
        params.from = fromAccount
        return tfc.deployed().then { instance ->
            instance.contract.methods.setNodeBody(nodeId, data).send(params)
        } as Promise<Json>
    }

    fun parentOf(parentId: Int): Promise<dynamic> {
        return tfc.deployed().then { instance ->
            instance.contract.methods.parentOf(parentId).call()
        }.then { res ->
            console.log(res)
        } as Promise<dynamic>
    }

    fun ownerOf(nodeId: JsObject): Promise<String> {
        return tfc.deployed().then { instance ->
            instance.contract.methods.ownerOf(nodeId).call()
        } as Promise<String>
    }

    fun nodeBody(nodeId: JsObject): Promise<String> {
        val response = tfc.deployed().then { instance ->
            instance.contract.methods.nodeBody(nodeId).call()
        }.catch { err ->
            console.error(err)
        }
        return response as Promise<String>
    }

    fun getPastEvents(eventName: String, selectedAccount: String? = null): Promise<dynamic> {
        val options = js("{filter:{sender:''},fromBlock: 0, toBlock: 'latest'}")
        selectedAccount.let {
            options.sender = selectedAccount
        }
        return tfc.deployed().then { instance ->
            instance.getPastEvents(eventName, options)
        }.catch { err ->
            console.error(err)
        } as Promise<dynamic>
    }
}

object URLShortenerContract {
    private var tfc: dynamic = null

    fun get(): URLShortenerContract {
        return tfc
    }

    init {
        coroutineAppScope.launch {
            create()
        }
    }

    suspend fun create() {
        val json = fetchContract("URLShortener.json").asDeferred().await()
        tfc = truffleContract(json)
        tfc.setProvider(Web3.get().currentProvider)
        console.log("Created URLShortener contract " + tfc)
    }

    fun claimAndSetNodeBody(label: ByteArray, body: ByteArray, fromAccount: String): Promise<Json> {
        val params = js("{from: '' }")
        params.from = fromAccount
        return tfc.deployed().then { instance ->
            instance.contract.methods.claimAndSetNodeBody(label, body).send(params)
        } as Promise<Json>
    }

    fun parentNodeId(): Promise<Int> {
        return tfc.deployed().then { instance ->
            instance.contract.methods.parentNodeId().call()
        } as Promise<Int>
    }
}

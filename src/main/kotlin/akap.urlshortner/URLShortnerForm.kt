package akap.urlshortner

import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.browser.document

interface URLShortnerFormState : RState {
    var longURL: String
    var urlShortLabel: String
    var selectedAccount: String
}

fun RBuilder.showLink(label: String) = if (label.isNotEmpty()) {
    div("form-group row") {
        attrs.jsStyle["padding-left"] = "1em"
        div("alert alert-primary") {
            val txt = "Here is your short link $redirectBase$label. "
            +txt
            a(href = "$redirectBase/#/r/$label", classes= "alert-link") { +"Check it out" }
        }
    }
} else {
    EmptyElement
}

class URLShortnerForm : RComponent<RProps, URLShortnerFormState>() {

    override fun URLShortnerFormState.init() {
        urlShortLabel = ""
        longURL = ""
    }

    override fun componentDidMount() {
        /* This is async block - but it should be safe to initialise here as componentDidMount blocks JS single thread */
        coroutineAppScope.launch {
            val accounts = Web3.getAccounts().asDeferred().await()
            setState {
                selectedAccount = accounts[0]
            }
            console.log(state.selectedAccount)
            AKAPContract.create()
        }
    }

    override fun RBuilder.render() {
        div ("bg-light p-2 rounded"){
            h2("AKAP URL Shortner"){
                +"AKAP URL Shortner"
            }
            p("lead"){
                +"Enter or paste your link, then click get short link to generate a short link."
            }

        }
        form {
            attrs.jsStyle["padding-left"] = "4em"
            attrs.jsStyle["padding-top"] = "1em"

            div("form-group row") {
                div("col-md-10") {
                    input(InputType.url, classes = "form-control mr-md-2") {
                        attrs {
                            id = "urlInput"
                            placeholder = "URL"
                            required = true
                            onChangeFunction = ::onInputChange
                        }
                        attrs["aria-label"] = "longURL"
                    }
                }

                button(type = ButtonType.submit, classes = "btn btn-outline-success my-2 my-sm-0") {
                    +"Get Short Link"
                    attrs {
                        onClickFunction = ::onGetShortLinkClick
                    }
                }
            }
            showLink(state.urlShortLabel)
            web3Alert()
        }
    }

    private fun onInputChange(event: Event) {
        val target = event.target as HTMLInputElement
        setState {
            longURL = target.value
        }
    }

    private fun onGetShortLinkClick(event: Event) {
        val urlInput = document.getElementById("urlInput") as HTMLInputElement
        if (urlInput.checkValidity()) {
            coroutineAppScope.launch {
                val label = createShortLink(state.longURL, state.selectedAccount)
                setState {
                    urlShortLabel = label
                }
            }
        }
    }
}
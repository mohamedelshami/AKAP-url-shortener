package akap.urlshortner

import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.UnionElementOrRadioNodeList
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
            +"Here is your short link: "
            a(href = "$redirectBase#$label", classes = "alert-link") { +"$redirectBase#$label" }
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
            Web3.enable().asDeferred().await()
            val accounts = Web3.getAccounts().asDeferred().await()
            setState {
                selectedAccount = accounts[0]
            }
            AKAPContract.create()
            URLShortenerContract.create()
            console.log("Selected Account " + state.selectedAccount)
        }
    }

    override fun RBuilder.render() {
        div("bg-light p-2 rounded") {
            h2("AKAP URL Shortner") {
                +"Redir.eth"
            }
            p("lead") {
                +"Enter or paste your link, then click 'Get Short Link' to generate an Ethereum managed short link."
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
        }
    }

    private fun onInputChange(event: Event) {
        event.preventDefault()
        val target = event.target as HTMLInputElement
        setState {
            longURL = target.value
        }
    }

    private fun onGetShortLinkClick(event: Event) {
        event.preventDefault()
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
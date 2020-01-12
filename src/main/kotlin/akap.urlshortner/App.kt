package akap.urlshortner

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*
import react.dom.*
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch
import kotlin.browser.document
import kotlin.browser.window

val coroutineAppScope = MainScope()

// TODO: Make Base Redirect URL configurable, this doesn't have to be the same web host
const val redirectBase = "http://localhost:8088"

object EmptyElement : ReactElement {
    override val props = object : RProps {}
}

fun RBuilder.web3Alert() = div {
    div("alert alert-warning") {
        +"No Web3 or Ethereum provider was detected in your browser. In order to connect to this site, please download one of the following plugins"
    }
    div("row") {
        div("missing-web3 w-25 p-3") {
            a(href = "https://metamask.io") {
                img(classes = "img-fluid", src = "img/metamask-logo.png") {}
            }
        }

        div("missing-web3 w-25 p-3") {
            a(href = "https://www.meetdapper.com") {
                img(classes = "img-fluid", src = "img/dapper-logo.png") {}
            }
        }
    }
}

interface LinkProp : RProps {
    var linkId: String
}

class App : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        if (Web3.isSupported()) {
            hashRouter {
                switch {
                    route<LinkProp>("/", exact = true) {
                        div {
                            child(NavigationView::class) {
                                attrs.items = listOf(
                                        NavigationItem("Home", "#", true),
                                        NavigationItem("My Links", "#mylinks"),
                                        NavigationItem("About", "https://github.com/mohamedelshami/AKAP-url-shortener")
                                )
                            }
                            div("container") {
                                child(URLShortnerForm::class) {}
                            }
                        }
                    }
                    route<LinkProp>("/:linkId") { props ->
                        if (props.match.params.linkId == "mylinks") {
                            div {
                                child(NavigationView::class) {
                                    attrs.items = listOf(
                                            NavigationItem("Home", "#"),
                                            NavigationItem("My Links", "#mylinks", true),
                                            NavigationItem("About", "https://github.com/mohamedelshami/AKAP-url-shortener")
                                    )
                                }
                                div("container") {
                                    child(MyLinksView::class) {}
                                }
                            }
                        }
                        else if (props.match.params.linkId != undefined) {
                            coroutineAppScope.launch {
                                AKAPContract.create()

                                val longURL = getURLFromLabel(props.match.params.linkId)

                                if (longURL != undefined)
                                    window.location.assign(longURL)
                            }
                            div("container") {
                                div { +"Redirecting.." }
                            }
                        }
                        else {
                            div {
                                child(NavigationView::class) {
                                    attrs.items = listOf(
                                            NavigationItem("Home", "#"),
                                            NavigationItem("My Links", "#mylinks"),
                                            NavigationItem("About", "https://github.com/mohamedelshami/AKAP-url-shortener")
                                    )
                                }
                                div("container") {
                                    div { +"No link was found for the given Id." }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            web3Alert()
        }
    }
}

fun RBuilder.app() = child(App::class) {}

fun main() {
    render(document.getElementById("root")) {
        app()
    }
}
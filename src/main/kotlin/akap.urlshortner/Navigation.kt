package akap.urlshortner

import kotlinx.html.ButtonType
import kotlinx.html.id
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

data class NavigationItem(val label: String, val link: String, val active: Boolean = false)

interface NavigationProps : RProps {
    var items: List<NavigationItem>
}

interface NavigationState : RState

class NavigationView(props: NavigationProps) : RComponent<NavigationProps, NavigationState>() {
    override fun RBuilder.render() {
        nav("navbar navbar-expand-md navbar-dark bg-dark mb-4") {
            div("container-fluid") {
                a(classes = "navbar-brand", href = "#") {}

                button(classes = "navbar-toggler", type = ButtonType.button) {
                    attrs["data-toggle"] = "collapse"
                    attrs["data-target"] = "#navbarCollapse"
                    attrs["aria-controls"] = "navbarCollapse"
                    attrs["aria-expanded"] = "false"
                    attrs["aria-label"] = "Toggle navigation"
                    span("navbar-toggler-icon") {}
                }

                div("collapse navbar-collapse") {
                    attrs {
                        id = "navbarCollapse"
                    }
                    ul("navbar-nav mr-auto") {
                        props.items.forEach {
                            if (it.active) {
                                li ("nav-item active") {
                                    a(classes = "nav-link", href="${it.link}") {
                                        +it.label
                                        span("sr-only") { +"(current)" }
                                    }
                                }
                            } else {
                                li ("nav-item") {
                                    a(classes = "nav-link", href="${it.link}") {
                                        +it.label
                                        span("sr-only") { +"(current)" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

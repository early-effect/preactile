package preactile.docs

import specular.client.Mounter

object StylingDemo:

  val mounter: Mounter = Mounter.sync(el => preactile.preact.render(App(()), el))

  // specular:begin demo
  import preactile.*
  import preactile.dsl.css.CssClass

  object CardStyles
      extends CssClass(
        S.background("#fff"),
        S.borderRadius("8px"),
        S.boxShadow("0 2px 4px rgba(0,0,0,0.1)"),
        S.padding("16px"),
        S.maxWidth("300px"),
      )

  object TitleStyles
      extends CssClass(
        S.marginBottom("8px"),
        S.color("#1f2937"),
      )

  object BodyStyles
      extends CssClass(
        S.color("#6b7280"),
        S.fontSize("14px"),
        S.lineHeight("1.5"),
      )

  object App extends Component[Unit]:
    def render(props: Unit): VNode = E.div(
      A.`class`("styling-demo"),
      E.div(
        CardStyles,
        E.h3(TitleStyles, "Styled Card"),
        E.p(BodyStyles, "This card uses Preactile's CSS-in-JS utilities for scoped styling."),
        E.div(
          A.style(S.marginTop("12px"), S.padding("8px"), S.background("#f3f4f6"), S.borderRadius("4px")),
          E.small("Inline styles work too!"),
        ),
      ),
    )
  end App
  // specular:end
end StylingDemo

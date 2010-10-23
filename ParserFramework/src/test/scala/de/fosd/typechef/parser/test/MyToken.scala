package de.fosd.typechef.parser.test
import de.fosd.typechef.parser._
import de.fosd.typechef.featureexpr.FeatureExpr

class MyToken(val text: String, val feature: FeatureExpr) extends AbstractToken {
    def t() = text
    def getText = text
    def getFeature = feature
    def getPosition = new Position {
        def getFile = "stream"
        def getLine = 1
        def getColumn = 1
    }

    override def toString = "\"" + text + "\"" + (if (!feature.isBase) feature else "")
}
object EofToken extends MyToken("EOF", FeatureExpr.base)
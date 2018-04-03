
package object spade extends spade.util.PrismAlias {
  type SpadeNode = spade.node.SpadeNode
  type SpadeDesign = spade.node.SpadeDesign
  type Module = spade.node.Module
  type PinType = spade.node.PinType
  type Pin[P<:PinType] = spade.node.Pin[P]
  type SpadePass = spade.pass.SpadePass
  type SpadeWorld = spade.pass.SpadeWorld
  type SpadeTraversal = spade.pass.SpadeTraversal
  type SpadeMapLike = spade.config.SpadeMapLike
  type SpadeMetadata = spade.util.SpadeMetadata
}

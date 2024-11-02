# Minecraft Code Breaking Changes
## 1.19.4
Current master

## 1.20.0
### No Workaround
- `DrawableHelper` is replaced with `DrawContext`
- `MatrixStack` parameters are replaced with `DrawContext` in most GUI.
- `drawTextureQuad` is no longer static and no longer takes a matrix stack as parameter
- `drawTextureQuad` now takes a texture ID as parameter; calling `RenderSystem.setShaderTexture` is no longer required in those places.
- `RenderSystem.enableBlend` needs to be called in more places.
- Vertex Consumer is now provided in the draw context.

## 1.20.5
### No Workaround
- `InGameHud::render` was completely revamped, requiring a new mixin injection point. On the bright side, the injection point in this version looks infinitely cleaner.

## 1.21
### No Workaround
- Main Hud render now takes a `RenderTickCounter`, instead of a straight up float.

## 1.21.2
- Most `DrawContext.draw_xxx` now require a render layer as parameters. Existing parameters were rearranged.
- The VertexConsummerProvider is no longer directly accessible for rendering text with outline.

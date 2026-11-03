# Your custom app icon

The crowned pen nib is a temporary vector mark until your artwork is ready.

1. Design on a square 1024 × 1024 canvas in Canva, Figma, Procreate, or another
   drawing tool. A clear photograph/scan of a paper drawing also works as a starting
   reference, but a clean PNG or SVG is best for the final asset.
2. Keep the important shape in the central two-thirds, with generous empty space
   around it. Android masks icons into circles, rounded squares, and other shapes.
3. Export a PNG, preferably with a transparent background. A vector SVG is also
   welcome. Supply the desired background color separately if possible.
4. Avoid tiny details and small lettering; the icon is normally seen at 48 dp.
   Do not draw rounded outer corners into the artwork; Android provides the mask.
5. Save it as `Downloads/app-icon.png` (or `.svg`) and share its local path in chat.

Android uses an adaptive background plus a foreground and a monochrome asset for
themed icons. The assets currently live in:

- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`

The app reuses the foreground mark on the opening screen and home header so your
final artwork can be applied consistently. The reference-photo collection is
separate from these branding assets.

# Introduction #
HoM has been designed so that it easy for users to create their own levels and share it with others.

However, note that level creation is suitable for slightly advanced users who
  * know the XML file format
  * know how to edit XML files

# File format #
Each level is stored in a separate file in XML format. This format makes it easy for the users to read/write and, yet, allows easy parsing by HoM.

Each level-file contains the following information
  * Level description (HTML formatted text)
  * Size of the grid
  * List of fixed-objects
  * List of movable-objects

The objects are called **gates** in HoM (because they control the flow of light). Examples of objects are **Mirror**, **PartialMirror**, **CrossMirror**, **Light Source** and **Detector**.

## Constraints ##
The following should be ensured by the level creator
  * Each gate should be in a different grid location
  * Gates should be within the bounds of the grid

These conditions are **not** checked by the code. The level designer needs to ensure them.

## Anatomy of a level file ##
Here is a sample level file
```
<houseofmirrors>

    <description>
        Text describing this level<br/><br/>You can use HTML markup here!
    </description>

    <bounds width="10" height="10" />

    <fixedGates>
        <Source color="red" direction="e" y="4" x="0"></Source>
        <Detector green="unwanted" red="required" blue="unwanted" y="1" x="4"></Detector>
    </fixedGates>

    <moveableGates>
        <Mirror direction="s" y="9" x="0"></Mirror>
    </moveableGates>

</houseofmirrors>
```

The `<bounds>` element specifies the grid size, that is number of rows and columns. For now, it is better to keep this a square (width == height) so that the objects don't look stretched.

The `<fixedGates>` and `<moveableGates>` are lists of gates. The format of the gates is described below (**TODO**).

# Steps to create a level #
  1. Start by copying an existing XML file as template
  1. Choose all the gates you want in the level and put in the xml file as moveable-gates.
  1. Make sure each one gets a unique position (the exact position is not important)
  1. Open it in the game (Game Menu -> Open)
  1. Use the game's GUI to place the gates in a solved position.
  1. Save it back to the file (Game Menu -> Save)
  1. Repeat previous steps till you get the level right
  1. Move all the moveable-gates to one corner of the house.
  1. Finally, move all the fixed gates under `<fixedGates>` and save the file
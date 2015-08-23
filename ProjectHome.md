**House Of Mirrors** (HoM) is a logic game inspired by [Chromatron](http://silverspaceship.com/chromatron/) (from Silver SpaceShip Software).

The game consists of a 2D grid in which several types of objects are found, including detectors and mirrors. The goal is to light up all the detectors using emitted or reflected rays of light. Usually, you can move and rotate the mirrors but not the detectors. In each level, the choice of available objects is limited. Note that there may be more than one solution for each level.

[Levels can be created](LevelCreation.md) by users and shared with others.

### Latest Version ###
HoM 1.0
> Support for Scala 2.9.
> Released on 11 Jan 2013

[Detailed Changelog](News.md)

### Minimum Requirements ###
HoM is written in [Scala](http://www.scala-lang.org) and uses Java-Swing for UI. Hence it will run on all platforms on which the following are available :
  * [Scala](http://www.scala-lang.org) Version 0.9 of Hom requires Scala 2.8. Version 0.8 requires Scala 2.7.1+
  * [Java 1.6](http://java.sun.com)

### Screenshot ###
![http://houseofmirrors.googlecode.com/files/hom0_4.png](http://houseofmirrors.googlecode.com/files/hom0_4.png)

### Differences from Chromatron ###
  * In HoM there are fewer restrictions on what can be rotated or moved. For example, some light-sources and detectors might be moveable, while there might be some mirrors in a level that are fixed! This leads to, IMO, more interesting game play.
  * HoM is multi-platform. It should work on all platforms supported by Java
  * HoM is open-source.

### How to Play ###
_For a more detailed help see GameHelp._

The goal is simple : light up all the detectors using emitted or reflected rays of light. Usually, you can move and rotate the mirrors but not the detectors. (Things that can't be moved have a dark circle around them)

You can play with the mouse as well as the keyboard.

Select an object by clicking on it with the mouse, or by pressing `N` or `P` keys. You can drag the object around with the mouse or use `UP, DOWN, LEFT, RIGHT` keys. If the piece can not be moved, the selection moves instead.

To rotate a piece in clockwise direction, click with the `LEFT` mouse button or press `SPACE`

To rotate a piece in counter-clockwise direction, click with the `RIGHT` mouse button or press `ENTER`

### Contact ###
Comments/Level creations may be sent to the Google group for HoM.

## Project Status ##

More work needs to be done in the following areas (_Priority_):
  * Add more types of elements in the house (_Medium_)
  * Create more levels (_Medium_)
  * Improve the graphics, probably using SVG images (_Medium_)
  * Level Editor (_Low_)
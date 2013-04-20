package hom

import java.awt.AWTEvent
import java.awt.event._
import javax.swing.JComponent
import java.awt.image.{BufferedImage}

/** Draws the House of Mirrors */
class HouseGui extends JComponent {
  import java.awt.{Graphics2D, Graphics, Color, BasicStroke, Dimension, Composite, RenderingHints, CompositeContext, GridBagConstraints}

  private val bgColor               = new Color(5, 5, 5)
  private val completedBgColor      = new Color(30, 0, 0)
  private val gridColor             = new Color(25, 25, 25)
  private val blockerColor          = new Color(42, 42, 42)
  private val blockerLightColor     = new Color(62, 62, 62)
  private val blockerDarkColor      = new Color(12, 12, 12)
  private val wormHoleColor         = new Color(28, 57, 13)
  private val wormHoleInnerColor    = new Color(42, 21, 21)
  private val wormHoleOuterColor    = new Color(150, 150, 150)
  private val boundColor            = new Color(10, 10, 50)
  private val mirrorColor           = new Color(120, 120, 140)
  private val mirrorBackColor       = new Color(40, 60, 60)
  private val crossMirrorColor      = new Color(90, 90 , 140)
  private val partMirrorColor       = new Color(100, 120, 140)
  private val partMirrorBorderColor = new Color(150, 150, 150)
  private val sourceColor           = new Color(150, 150, 150)
  private val conduitColor          = new Color(100, 100, 100)
  private val selectionColor        = new Color(200, 200, 200)
  private val dragColor             = new Color(100, 100, 150)
  private val dragLineColor         = new Color(50, 50, 50)

  private val whiteLineColor        = new Color(255, 255, 255)
  private val redLineColor          = new Color(255, 0, 0)
  private val greenLineColor        = new Color(0,255,0)
  private val blueLineColor         = new Color(0,0,255)

  private val widePixels    = 4
  private val wideStroke    = new BasicStroke(4)
  private val thinPixels    = 1
  private val thinStroke    = new BasicStroke(1)
  private val normStroke    = new BasicStroke(2)
  private val mediumStroke  = new BasicStroke(3)
  private val thickStroke   = new BasicStroke(6)

  private val handleShape   = {
    val path = new java.awt.geom.Path2D.Float
    path.moveTo(0.4,  0)
    path.lineTo(0.6,  0)
    path.lineTo(0.6,  0.5)
    path.lineTo(-0.6, 0.5)
    path.lineTo(-0.6, 0)
    path.lineTo(-0.4, 0)
    path.lineTo(-0.4, 0.3)
    path.lineTo(0.4,  0.3)
    path.lineTo(0.4,  0)
    path
  }

  private val selectionStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f, (2f :: 5f :: Nil).toArray, 0.0f)
  private val dragStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f, (1f :: 5f :: Nil).toArray, 0.0f)
  private var currentSelection = Point(-1,-1)
  private var dragStart = Point(-1,-1)
  private var hoverPoint = Point(-1,-1)

  var arenaModified = true
  private var arenaTrace = arena.trace

  enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK )

  /* Gives the nexpoint in the this direction */
  final private def nextPoint (p : Point, d : Direction.Value, hscale : Int, vscale : Int) = {
      val htilt = (0.7071067811865475 * hscale).toInt
      val vtilt = (0.7071067811865475 * vscale).toInt
      d match {
          case Direction.n => Point(p.x, p.y - vscale)
          case Direction.ne => Point(p.x + htilt, p.y - vtilt)
          case Direction.e => Point(p.x + hscale, p.y)
          case Direction.es => Point(p.x + htilt, p.y + vtilt)
          case Direction.s => Point(p.x, p.y + vscale)
          case Direction.sw => Point(p.x - htilt, p.y + vtilt)
          case Direction.w => Point(p.x - hscale, p.y)
          case Direction.wn => Point(p.x - htilt, p.y - vtilt)
      }
  }

  final private def drawLine(g : Graphics2D, p1 : Point, p2 : Point) = {
      g.drawLine(p1.x, p1.y, p2.x, p2.y)
  }

  final private def drawPartialMirror (g : Graphics2D, m : PartialMirror, p : Point, hscale : Int, vscale : Int) {
      val faceDirection = Direction.left90(m.direction)

      val point1 = nextPoint(p, faceDirection, hscale/3, vscale/3)
      val point2 = nextPoint(p, Direction.reverse(faceDirection), hscale/3, vscale/3)

      g.setColor(partMirrorColor)
      g.setStroke(wideStroke)
      drawLine(g, point1, point2)

      val point1edge = nextPoint(p, faceDirection, hscale/3 + widePixels/2, vscale/3 + widePixels/2)
      val point2edge = nextPoint(p, Direction.reverse(faceDirection), hscale/3 + widePixels/2, vscale/3 + widePixels/2)
      g.setColor(partMirrorBorderColor)
      g.setStroke(normStroke)
      drawLine(g, nextPoint(point1edge, Direction.right90(faceDirection), widePixels, widePixels),
                  nextPoint(point1edge, Direction.left90(faceDirection), widePixels, widePixels))
      drawLine(g, nextPoint(point2edge, Direction.right90(faceDirection), widePixels, widePixels),
                  nextPoint(point2edge, Direction.left90(faceDirection), widePixels, widePixels))


  }

  final private def drawSource (g : Graphics2D, s : Source, p : Point, hscale : Int, vscale : Int) {
      val centerOval = nextPoint(p, Direction.reverse(s.direction), hscale/4, vscale/4)
      val leftOfCenter = nextPoint(centerOval, Direction.left90(s.direction), hscale/8, vscale/8)
      val rightOfCenter = nextPoint(centerOval, Direction.right90(s.direction), hscale/8, vscale/8)

      g.setColor(sourceColor)
      g.setStroke(thinStroke)
      g.fillOval(centerOval.x - hscale/8, centerOval.y - vscale/8, hscale/4, vscale/4)
      drawLine(g, leftOfCenter, nextPoint(leftOfCenter, s.direction, hscale/2, vscale/2))
      drawLine(g, rightOfCenter, nextPoint(rightOfCenter, s.direction, hscale/2, vscale/2))

      g.setColor(getDisplayColor(s.color))
      g.fillOval(centerOval.x - hscale/10, centerOval.y - vscale/10, hscale/5, vscale/5)
  }

  final private def drawBlocker (g : Graphics2D, b : Blocker, p : Point, hscale : Int, vscale : Int) {
      g.setColor(blockerColor)
      val topLeft     = Point(p.x - hscale/2 + thinPixels, p.y - vscale/2 + thinPixels)
      val topRight    = Point(p.x + hscale/2 - thinPixels, p.y - vscale/2 + thinPixels)
      val bottomRight = Point(p.x + hscale/2 - thinPixels, p.y + vscale/2 - thinPixels)
      val bottomLeft  = Point(p.x - hscale/2 + thinPixels, p.y + vscale/2 - thinPixels)

      g.fillRect(topLeft.x - thinPixels, topLeft.y - thinPixels, hscale, vscale)

      g.setStroke(thinStroke)
      g.setColor(blockerLightColor)
      drawLine(g, topLeft, topRight)
      drawLine(g, topLeft, bottomLeft)

      g.setStroke(thinStroke)
      g.setColor(blockerDarkColor)
      drawLine(g, topRight, bottomRight)
      drawLine(g, bottomLeft, bottomRight)
  }

  val white = new Color(255, 255, 255)
  val red = new Color(255, 0, 0)
  val green = new Color(0, 255, 0)
  val blue = new Color(0, 0, 255)
  val yellow = new Color(255, 255, 0)
  val teal = new Color(0, 255, 255)
  val purple = new Color(255, 0, 255)

  final private def drawDetector (g : Graphics2D, d : Detector, p : Point, hscale : Int, vscale : Int) {

      val redWanted = d.colorNeeds.red != ColorNeed.unwanted
      val greenWanted = d.colorNeeds.green != ColorNeed.unwanted
      val blueWanted = d.colorNeeds.blue != ColorNeed.unwanted
      val detectorColor  = if (redWanted && greenWanted && blueWanted) {
          white
      } else if (redWanted && greenWanted) {
          yellow
      } else if (redWanted && blueWanted) {
          purple
      } else if (greenWanted && blueWanted) {
          teal
      } else if (redWanted) {
          red
      } else if (greenWanted) {
          green
      } else if (blueWanted) {
          blue
      } else {
          bgColor
      }


      g.setStroke(thickStroke)
      if (d.on) {
          g.setColor(detectorColor)
          g.drawOval(p.x - hscale/4, p.y - vscale/4, hscale/2, vscale/2)
          g.fillOval(p.x - hscale/4, p.y - vscale/4, hscale/2, vscale/2)
      } else {
          g.setColor(detectorColor.darker.darker)
          g.drawOval(p.x - hscale/4, p.y - vscale/4, hscale/2, vscale/2)
      }

      g.setStroke(normStroke)
      g.setColor(gridColor)
      g.drawOval(p.x - hscale/4 - 1, p.y - vscale/4 - 1, hscale/2 + 2, vscale/2 + 2)
      drawLine(g, nextPoint(p, Direction.ne, hscale/4, vscale/4),
              nextPoint(p, Direction.sw, hscale/4, vscale/4))
      drawLine(g, nextPoint(p, Direction.es, hscale/4, vscale/4),
              nextPoint(p, Direction.wn, hscale/4, vscale/4))
      
  }

  final private def drawCrossMirror (g : Graphics2D, m : CrossMirror, p : Point, hscale : Int, vscale : Int) {

      val angle = (Direction.angle(Direction.e, m.direction) + 22.5).toRadians
      val complement = (90 - Direction.angle(Direction.e, m.direction) - 22.5).toRadians
      val hlen = (hscale * Math.sin(angle)).toInt / 3
      val vlen = (hscale * Math.cos(angle)).toInt / 3

      // Draw the back plane
      g.setColor(mirrorBackColor)
      val scaleTransform = java.awt.geom.AffineTransform.getScaleInstance(hscale/2,vscale/2)
      val transTransform = java.awt.geom.AffineTransform.getTranslateInstance(p.x,p.y)
      val rotTransform   = java.awt.geom.AffineTransform.getRotateInstance(complement)
      val modShape = scaleTransform.createTransformedShape(handleShape)
      val modShape2 = rotTransform.createTransformedShape(modShape)
      val modShape3 = transTransform.createTransformedShape(modShape2)
      g.fill(modShape3)
/*
      g.setStroke(thinStroke)
      val backP = nextPoint(p, Direction.reverse(m.direction), hscale/6, vscale/6)
      drawLine(g, Point(backP.x + hlen, backP.y + vlen), Point (backP.x - hlen, backP.y - vlen))
      val backP2 = nextPoint(p, Direction.reverse(m.direction), hscale/6 - 2, vscale/6 - 2)
      drawLine(g, Point(backP2.x + hlen, backP2.y + vlen), Point (backP2.x - hlen, backP2.y - vlen))
*/

      g.setColor(crossMirrorColor)
      g.setStroke(mediumStroke)

      drawLine(g, Point(p.x + hlen, p.y + vlen), Point (p.x - hlen, p.y - vlen))
      
  }

  final private def drawPrism (g : Graphics2D, p : Prism, inPoint : Point, hscale : Int, vscale : Int) {
      val faceDirection = Direction.left90(p.direction)
      val topPoint = nextPoint(inPoint, Direction.reverse(p.direction), hscale/4, vscale/4)
      val bottomPoint = nextPoint(inPoint, p.direction, hscale/4, vscale/4)
      val bottomLeftPoint = nextPoint(bottomPoint, faceDirection, hscale/3, vscale/3)
      val bottomRightPoint = nextPoint(bottomPoint, Direction.reverse(faceDirection), hscale/3, vscale/3)

      g.setColor(mirrorColor)
      g.setStroke(mediumStroke)
      drawLine(g, bottomLeftPoint, bottomRightPoint)
      g.setStroke(thinStroke)
      drawLine(g, bottomLeftPoint, topPoint)
      drawLine(g, bottomRightPoint, topPoint)
  }

  final private def drawMirror (g : Graphics2D, m : Mirror, p : Point, hscale : Int, vscale : Int) {
      val faceDirection = Direction.left90(m.direction)

      g.setColor(mirrorColor)
      g.setStroke(mediumStroke)
      drawLine(g, nextPoint(p, faceDirection, hscale/3, vscale/3),
              nextPoint(p, Direction.reverse(faceDirection), hscale/3, vscale/3))

      // Draw the back plane
      g.setColor(mirrorBackColor)
      g.setStroke(thinStroke)
      val backP = nextPoint(p, Direction.reverse(m.direction), hscale/6, vscale/6)
      drawLine(g, nextPoint(backP, faceDirection, hscale/3, vscale/3),
              nextPoint(backP, Direction.reverse(faceDirection), hscale/3, vscale/3))
      val backP2 = nextPoint(p, Direction.reverse(m.direction), hscale/6 - 2, vscale/6 - 2)
      drawLine(g, nextPoint(backP2, faceDirection, hscale/3, vscale/3),
              nextPoint(backP2, Direction.reverse(faceDirection), hscale/3, vscale/3))
  }

  final private def drawConduit (g : Graphics2D, c : Conduit, p : Point, hscale : Int, vscale : Int) {
      val sideDirection = Direction.left90(c.direction)
      // Draw the sides
      g.setColor(conduitColor)
      g.setStroke(thinStroke)
      val backP = nextPoint(p, sideDirection, hscale/8, vscale/8)
      drawLine(g, nextPoint(backP, c.direction, hscale/3, vscale/3),
              nextPoint(backP, Direction.reverse(c.direction), hscale/3, vscale/3))
      val backP2 = nextPoint(p, Direction.reverse(sideDirection), hscale/8, vscale/8)
      drawLine(g, nextPoint(backP2, c.direction, hscale/3, vscale/3),
              nextPoint(backP2, Direction.reverse(c.direction), hscale/3, vscale/3))
  }

  final private def drawWormHole (g : Graphics2D, c : WormHole, p : Point, hscale : Int, vscale : Int) {
      g.setColor(wormHoleOuterColor)
      g.setStroke(normStroke)
      g.drawOval(p.x - (3*hscale)/8, p.y - (3*vscale)/8, 3*hscale/4, 3*vscale/4)

      g.setColor(wormHoleColor)
      g.setStroke(normStroke)
      g.drawOval(p.x - hscale/4, p.y - vscale/4, hscale/2, vscale/2)

      g.setColor(wormHoleInnerColor)
      g.setStroke(mediumStroke)
      g.drawOval(p.x - hscale/8, p.y - vscale/8, hscale/4, vscale/4)

      // g.fillRect(p.x - 3*hscale/8, p.y - 3*vscale/8, 3*hscale/4, 3*vscale/4)
      // g.fillOval(p.x - hscale/4, p.y - vscale/4, hscale/2, vscale/2)
  }

  final private def drawGates (g : Graphics2D, gates : List[Gate], hscale:Int, vscale:Int) {
      gates.foreach (gate => {
              val p = Point(hscale/2 + gate.position.x * hscale, vscale/2 + gate.position.y * vscale)

              arena.fixed_gates.find(g => g.position == gate.position).foreach { _ =>
                  // Draw some bolts around it. For now, just an oval will do
                  g.setColor(gridColor)
                  g.setStroke(thinStroke)
                  g.drawOval(p.x - hscale/2, p.y - hscale/2, hscale, vscale)
              }

              gate match {
              case m:Mirror         => drawMirror(g, m, p, hscale, vscale)
              case pm:PartialMirror => drawPartialMirror(g, pm, p, hscale, vscale)
              case cm:CrossMirror   => drawCrossMirror(g, cm, p, hscale, vscale)
              case d:Detector       => drawDetector(g, d, p, hscale, vscale)
              case s:Source         => drawSource(g, s, p, hscale, vscale)
              case b:Blocker        => drawBlocker(g, b, p, hscale, vscale)
              case prism:Prism      => drawPrism(g, prism, p, hscale, vscale)
              case c:Conduit        => drawConduit(g, c, p, hscale, vscale)
              case w:WormHole       => drawWormHole(g, w, p, hscale, vscale)
              }

          }
      )
  }

  override def processMouseMotionEvent (e : MouseEvent ) = {
    if ((dragStart.x != -1) && (dragStart.y != -1)) {
      val d = getSize()
      val (_, hscale, vscale) = calcScale(d)
      val newHoverPoint = Point((e.getPoint.getX / hscale).toInt, (e.getPoint.getY / vscale).toInt)
      if (newHoverPoint != hoverPoint) {
        hoverPoint = newHoverPoint
        this.repaint(300)
      }
    }
  }

  override def processMouseEvent (e : MouseEvent ) = {
      if ((e.getID == MouseEvent.MOUSE_PRESSED)) {
          val d = getSize()
          val (_, hscale, vscale) = calcScale(d)
          val clickPoint = Point((e.getPoint.getX / hscale).toInt, (e.getPoint.getY / vscale).toInt)
          if (!arena.fixed_gates.find(_.position == clickPoint).isDefined) {
            dragStart = clickPoint
            this.repaint(100)
          }
      } else if ((e.getID == MouseEvent.MOUSE_RELEASED)) {
          val d = getSize()
          val (_, hscale, vscale) = calcScale(d)
          val releasePoint = Point((e.getPoint.getX / hscale).toInt, (e.getPoint.getY / vscale).toInt)

          if (dragStart == releasePoint) {
              // This was just a click
              if (releasePoint == currentSelection) {
                  if (e.getButton == 1) {
                      arena.turnCCW(currentSelection)
                  } else {
                      arena.turnCW(currentSelection)
                  }
                  arenaModified = true
              } else {
                  currentSelection = releasePoint
              }
          } else {
              // This was a drag :)
              if (arena.bounds.contains(releasePoint)) {
                if (!arena.fixed_gates.find(_.position == releasePoint).isDefined) {
                  arena.moveTo (dragStart, releasePoint)
                  arenaModified = true
                  currentSelection = releasePoint
                }
              }
          }

          dragStart = Point(-1, -1)

          this.repaint(100)
      }
  }

  override def processKeyEvent (e : KeyEvent ) = {
      if (e.getID == KeyEvent.KEY_PRESSED) {
          e.getKeyCode match {
              case KeyEvent.VK_SPACE => {
                  arena.turnCCW(currentSelection)
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_ENTER => {
                  arena.turnCW(currentSelection)
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_UP => {
                  arena.moveUp(currentSelection)
                  currentSelection = if (arena.bounds.contains(currentSelection.moveUp)) {
                      currentSelection.moveUp
                  } else currentSelection
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_DOWN => {
                  arena.moveDown(currentSelection)
                  currentSelection = if (arena.bounds.contains(currentSelection.moveDown)) {
                      currentSelection.moveDown
                  } else currentSelection
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_LEFT => {
                  arena.moveLeft(currentSelection)
                  currentSelection = if (arena.bounds.contains(currentSelection.moveLeft)) {
                      currentSelection.moveLeft
                  } else currentSelection
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_RIGHT => {
                  arena.moveRight(currentSelection)
                  currentSelection = if (arena.bounds.contains(currentSelection.moveRight)) {
                      currentSelection.moveRight
                  } else currentSelection
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_N => {
                  arena.moveable_gates.zipWithIndex.find(g => g._1.position == currentSelection) match {
                      case Some(currentGate) => {
                          val currentIndex = currentGate._2
                          val nextIndex = if (currentIndex == arena.moveable_gates.length - 1) 0 else currentIndex + 1
                          currentSelection = arena.moveable_gates(nextIndex).position
                      }
                      case None => {
                          currentSelection = arena.moveable_gates(0).position
                      }
                  }
                  this.repaint(100)
              }
              case KeyEvent.VK_P => {
                  arena.moveable_gates.zipWithIndex.find(g => g._1.position == currentSelection) match {
                      case Some(currentGate) => {
                          val currentIndex = currentGate._2
                          val nextIndex = if (currentIndex == 0) arena.moveable_gates.length - 1 else currentIndex - 1
                          currentSelection = arena.moveable_gates(nextIndex).position
                      }
                      case None => {
                          currentSelection = arena.moveable_gates(0).position
                      }
                  }
                  this.repaint(100)
              }
              case KeyEvent.VK_PAGE_DOWN => {
                  arena.nextLevel
                  currentSelection = arena.moveable_gates(0).position
                  arenaModified = true
                  this.repaint(100)
              }
              case KeyEvent.VK_PAGE_UP => {
                  arena.prevLevel
                  currentSelection = arena.moveable_gates(0).position
                  arenaModified = true
                  this.repaint(100)
              }
              case _ => null // println ("Key not supported: " + e.getKeyCode)
          }
          // println ("Key : " + KeyEvent.getKeyText(e.getKeyCode))
      }
  }


  final private def getDisplayColor (color : LineColor.Value) = color match {
      case LineColor.w => whiteLineColor
      case LineColor.r => redLineColor
      case LineColor.g => greenLineColor
      case LineColor.b => blueLineColor
  }

  var offscreen = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB)

  override def paint(g: Graphics):Unit = {
      val d = getSize()
      if ((d.width != offscreen.getWidth) || (d.height != offscreen.getHeight)) {
          offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB)
      }
      val bufferedGraphics = offscreen.getGraphics
      // bufferedGraphics.setColor (bgColor)
      // dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);
      paintHouse(bufferedGraphics)
      g.drawImage(offscreen, 0, 0, this)
  }

  final private def calcScale(d:java.awt.Dimension) = {
      val minLength = if (d.width < d.height) d.width else d.height
      val hscale = minLength / arena.bounds.width
      val vscale = minLength / arena.bounds.height
      (minLength, hscale, vscale)
  }

  final private def paintHouse(oldg: Graphics):Unit = {
      val g = oldg.asInstanceOf[Graphics2D]
      val dim = getSize()
      val (minLength, hscale, vscale) = calcScale(dim)

      // Get the traces
      if (arenaModified) {arenaModified = false; arenaTrace = arena.trace}
      val (listLines, listGates) = arenaTrace
      val (statusMsg, completed) = arena.status(listGates)
      Main.statusBar.text = ("<html>" + arena.description + "     " + statusMsg + "</html>")
      Main.statusIcon.enabled = completed

      if (completed) arena.unlockLevels

      // Fill with Bg
      if (completed) g.setColor (completedBgColor) else g.setColor(bgColor)
      g.fillRect(0,0, minLength, minLength)

      g.setClip(0,0,arena.bounds.width * hscale - 1,arena.bounds.height * vscale - 1)


      // Draw the Grid
      g.setColor (gridColor)
      g.setStroke(thinStroke)
      (0 until arena.bounds.width).foreach {i =>
         g.drawLine(i * hscale, 0, i * hscale, minLength)
      }

      (0 until arena.bounds.height).foreach {i =>
        g.drawLine(0, i * vscale, minLength, i * vscale)
      }

      // Draw those saber rays
      val oldComposite = g.getComposite
      g.setComposite(AddComposite)
      g.setStroke(thinStroke)
      listLines foreach {l =>
          g.setColor (getDisplayColor(l.color))
          g.drawLine (hscale/2 + l.start.x * hscale,
                      vscale/2 + l.start.y * vscale,
                      hscale/2 + l.end.x * hscale,
                      // Math.min(hscale/2 + l.end.x * hscale, d.width - (d.width % hscale)),
                      vscale/2 + l.end.y * vscale)
      }
      g.setComposite(oldComposite)

      // Draw the gates. Notice that we draw them on-top-of the saber rays
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      drawGates (g, listGates, hscale, vscale)

      // Draw the boundary
      g.setStroke(thickStroke)
      g.setColor (boundColor)
      g.drawRect(arena.bounds.topleft.x * hscale,
              arena.bounds.topleft.y * vscale,
              arena.bounds.botright.x * hscale,
              arena.bounds.botright.y * vscale)

      // Draw the selection
      if ((currentSelection != Point(-1,-1)) && (currentSelection != dragStart)) {
        g.setStroke(selectionStroke)
        g.setColor (selectionColor)
        g.drawRect(currentSelection.x * hscale, currentSelection.y * vscale,
                hscale, vscale)
      }

      // Draw the drag-start and hover cells
      if (dragStart != Point(-1,-1)) {
        g.setStroke(dragStroke)
        g.setColor (dragColor)
        g.drawRect(dragStart.x * hscale, dragStart.y * vscale,
                hscale, vscale)

        g.setStroke(selectionStroke)
        g.drawRect(hoverPoint.x * hscale, hoverPoint.y * vscale,
                hscale, vscale)

        g.setColor (dragLineColor)
        // Draw an arrow to show what is moving where
        g.drawLine (hscale/2 + dragStart.x * hscale,
                    vscale/2 + dragStart.y * vscale,
                    hscale/2 + hoverPoint.x * hscale,
                    vscale/2 + hoverPoint.y * vscale)
      }

  }
}

import java.awt.{CompositeContext, RenderingHints}
import java.awt.image.{ColorModel, ComponentColorModel, Raster, WritableRaster, IndexColorModel, PackedColorModel}

object AddComposite extends java.awt.Composite {
    class AddCompositePackedContext extends CompositeContext {
        def compose (src : Raster, dst : Raster, w : WritableRaster) = {
            val minX = Math.max (src.getMinX, dst.getMinX)
            val maxX = minX + Math.min(src.getWidth, dst.getWidth)
            val minY = Math.max (src.getMinY, dst.getMinY)
            val maxY = minY + Math.min(src.getHeight, dst.getHeight)
            for (i <- minX until maxX; j <- minY until maxY; band <- 0 until Math.min(src.getNumBands, dst.getNumBands)) {
                val value = Math.min(src.getSample(i,j,band) + dst.getSample(i,j,band), 255)
                w.setSample(i, j, band, value)
            }
        }
        def dispose = {}
    }

    def createContext (cm : ColorModel, cm2 : ColorModel, rh : RenderingHints) : CompositeContext = new AddCompositePackedContext
}


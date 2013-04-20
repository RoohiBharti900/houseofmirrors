package hom

import java.io.{File,FileWriter}
import scala.xml._

object arena {
    var bounds = Bound(Point(0,0), Point(15,15))
    var description = ""

    var fixed_gates : List[Gate] =   Nil

    var moveable_gates : List[Gate] = Nil

    def gates = fixed_gates ::: moveable_gates

    def occupied (p : Point ) = {
        gates.find(g => g.position == p).isDefined
    }

    def turnCCW (p : Point) = {
        moveable_gates = moveable_gates.map ( g => if (g.position == p) g.turnCCW else g )
    }

    def turnCW (p : Point) = {
        moveable_gates = moveable_gates.map ( g => if (g.position == p) g.turnCW else g )
    }

    def moveUp (p : Point) = {
        moveable_gates = moveable_gates.map ( g =>
        if ((g.position == p) && (!occupied(g.position.moveUp))) {
            if (bounds.contains(g.position.moveUp)) g.moveUp else g
        } else g)
    }

    def moveDown (p : Point) = {
        moveable_gates = moveable_gates.map ( g =>
        if ((g.position == p) && (!occupied(g.position.moveDown))) {
            if (bounds.contains(g.position.moveDown)) g.moveDown else g
        } else g)
    }

    def moveLeft (p : Point) = {
        moveable_gates = moveable_gates.map ( g =>
        if ((g.position == p) && (!occupied(g.position.moveLeft))) {
            if (bounds.contains(g.position.moveLeft)) g.moveLeft else g
        } else g)
    }

    def moveRight (p : Point) = {
        moveable_gates = moveable_gates.map ( g =>
        if ((g.position == p) && (!occupied(g.position.moveRight))) {
            if (bounds.contains(g.position.moveRight)) g.moveRight else g
        } else g)
    }

    def moveTo (from : Point, to : Point) = {
        if (!occupied(to)) {
            moveable_gates = moveable_gates.map(g => if (g.position != from) g else g.moveTo(to))
        }
    }
    def sources = gates.filter(_.isInstanceOf[Source]).map(_.asInstanceOf[Source]).flatMap(source => {
        if (source.color == LineColor.w) {
            Ray(source.position,source.direction, LineColor.r) ::
            Ray(source.position,source.direction, LineColor.g) ::
            Ray(source.position,source.direction, LineColor.b) :: Nil
        } else {
            Ray(source.position,source.direction, source.color) :: Nil
        }
    })

    def trace = {
        var traceSegment : List[Segment] = Nil
        var traceGate : List[Gate] = gates
        sources.foreach (s => {
            val trace = s.shootRay(bounds, traceGate, Nil)
            traceSegment = trace._1 ::: traceSegment
            traceGate = trace._2
        })

        (traceSegment, traceGate)
    }

    def toXML = {
        <houseofmirrors >
            <description>{Unparsed(arena.description)}</description>
            <bounds width={bounds.width.toString} height={bounds.height.toString} />
            <fixedGates>{fixed_gates.map(g => g.toXML)}</fixedGates>
            <moveableGates>{moveable_gates.map(g => g.toXML)}</moveableGates>
        </houseofmirrors>
    }

    var currentLevel = 0
    var currentPackFile : Option[File] = None
    var currentPack = <HoMPack/>

    var unlockedLevels : List[Int] = Nil

    def unlockLevels = {
        if (currentPackFile.isDefined) {
            val currentLevelNode = (currentPack \\ "level").find(x => x.attribute("id").get.text.toInt == currentLevel).get
            (currentLevelNode \\ "unlock").foreach(x => {
                val u = x.attribute("id").get.text.toInt
                unlockedLevels ::= u
            })
            unlockedLevels = unlockedLevels.removeDuplicates

            val packXML = <HoMPack>
                        <unlocked> {unlockedLevels.map(u => <ulevel id={u.toString}/>)} </unlocked>
                        {currentPack \\ "level"}
                      </HoMPack>
            
            // Write to File
            val fileWriter = new FileWriter(currentPackFile.get)
            fileWriter.write(arena.prettyPrinter.format(packXML))
            fileWriter.close

            currentPack = packXML
        }
    }
    def loadPack (f : File) = {
        currentPackFile = Some(f)
        currentPack = XML.loadFile(f)
        unlockedLevels = (currentPack \\ "unlocked" \\ "ulevel").toList.map(x=>x.attribute("id").get.text.toInt)
        loadLevel (0)
    }

    def loadLevel (id : Int) = {
        if (currentPackFile.isDefined) {
            if (unlockedLevels.exists(_ == id)) {
                val requestedLevel = (currentPack \\ "level").find(x => x.attribute("id").get.text.toInt == id)
                if (requestedLevel.isDefined) {
                    val parentDir = currentPackFile.get.getParentFile.getAbsolutePath + File.separator
                    fromXML (XML.loadFile(parentDir + requestedLevel.get.attribute("file").get.text))
                    currentLevel = id
                }
            }
        }
    }

    def nextLevel = loadLevel (currentLevel + 1)
    def prevLevel = loadLevel (currentLevel - 1)

    def fromXML (topElem : Elem) = {
        def getColorNeed (node : Node, attr : String) = node.attribute(attr).get.text match {
            case "required" => ColorNeed.req
            case "met" => ColorNeed.met
            case "unwanted" => ColorNeed.unwanted
        }
        def getNum (node : Node, attr : String) = node.attribute(attr).get.text.toInt
        def getColor (node : Node) = node.attribute("color").get.text match {
            case "white" => LineColor.w
            case "red" => LineColor.r
            case "green" => LineColor.g
            case "blue" => LineColor.b
        }
        def getDirection (node : Node) = node.attribute("direction").get.text match {
            case "n" => Direction.n
            case "e" => Direction.e
            case "s" => Direction.s
            case "w" => Direction.w
            case "ne" => Direction.ne
            case "es" => Direction.es
            case "sw" => Direction.sw
            case "wn" => Direction.wn
        }
        val trimTop = scala.xml.Utility.trim(topElem)

        def parseGates (typeName : String) = (trimTop \\ typeName)(0).descendant.flatMap ( _ match {
            case s @ <Source/> => Some(Source(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s), getColor(s)))
            case s @ <Conduit/> => Some(Conduit(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s)))
            case s @ <Mirror/> => Some(Mirror(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s)))
            case s @ <Prism/> => Some(Prism(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s)))
            case s @ <PartialMirror/> => Some(PartialMirror(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s)))
            case s @ <CrossMirror/> => Some(CrossMirror(Point(getNum(s,"x"), getNum(s,"y")), getDirection(s)))
            case s @ <Detector/> => Some(Detector(Point(getNum(s,"x"), getNum(s,"y")), false, ColorNeeds(getColorNeed(s,"red"), getColorNeed(s,"green"), getColorNeed(s,"blue")), false))
            case s @ <Blocker/> => Some(Blocker(Point(getNum(s,"x"), getNum(s,"y"))))
            case s @ <WormHole/> => {
                val master = WormHole(Point(getNum(s,"x1"), getNum(s,"y1")), true, null)
                val slave = WormHole(Point(getNum(s,"x2"), getNum(s,"y2")), false, master)
                List (master.setTwin(slave), slave)
            }
        })

        val myBoundsNode = (trimTop \\ "bounds")(0)
        bounds = Bound(Point(0,0), Point (getNum(myBoundsNode, "width"), getNum(myBoundsNode, "height")))

        fixed_gates = parseGates ("fixedGates")
        moveable_gates = parseGates ("moveableGates")

        description = (trimTop \\ "description")(0).child.foldLeft("")(_ + _.toString)
        Main.statusBar.text = ("<html>" + description + "     " + status(trace._2) + "</html>")
        // statusBar.invalidate
    }

    def status (gates : List[Gate]) = {
        val detectors = gates.filter (_.isInstanceOf[Detector]).map(_.asInstanceOf[Detector])
        val numOn = detectors.foldLeft(0)((sum, d) => if (d.on) sum + 1 else sum)
        val totalDetectors = detectors.length
        val completed = numOn == totalDetectors
        ("""<br/><br/><hr/><font size="+1">Progress</font><br/> """ + numOn + " of " + detectors.length + (if (completed) " [Completed!]" else ""), completed)
    }

    val prettyPrinter = new PrettyPrinter(120,4)
}

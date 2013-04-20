package hom

/* A gate is the base class for all objects in the arena. It is called so because, all
 * objects in the arena manipulate light 'flow' */
class Gate (var position : Point) {
    def act(inRay : Ray) : (List[Ray], Gate) = (Nil, null)
    def turnCW = this
    def turnCCW = this
    def moveUp = {position = position.moveUp; this}
    def moveDown = {position = position.moveDown; this}
    def moveLeft = {position = position.moveLeft; this}
    def moveRight = {position = position.moveRight; this}
    def moveTo(dst : Point) = {position = dst; this}

    def toXML = <gate/>
}

case class WormHole (positionI : Point, isMaster:Boolean, var twin:WormHole) extends Gate(positionI) {
    override def act(inRay : Ray) = (Ray(twin.position, inRay.d, inRay.c) :: Nil, this)
    override def turnCW = this
    override def turnCCW = this
    override def moveTo(dst : Point) = {super.moveTo(dst);}

    def setTwin(newTwin:WormHole) = { twin = newTwin; this }

    override def toXML = if (isMaster) {
      <WormHole
        x1={position.x.toString} y1={position.y.toString}
        x2={twin.position.x.toString} y2={twin.position.y.toString}/>
    } else {
      <Ignore />
    }
}

case class Blocker (positionI : Point) extends Gate(positionI) {
    override def act(inRay : Ray) : (List[Ray], Gate) = (Nil, this)
    override def turnCW = this
    override def turnCCW = this

    override def toXML = <Blocker x={position.x.toString} y={position.y.toString} />
}

case class Conduit (positionI : Point, direction : Direction.Value) extends Gate (positionI) {
    override def act (inRay : Ray) =    if ((inRay.d == direction) || (inRay.d == Direction.reverse(direction)))
                                            (Ray(position, inRay.d, inRay.c) :: Nil, this)
                                        else (Nil, this)
    override def toXML = <Conduit x={position.x.toString} y={position.y.toString} direction={direction.toString} />
    override def turnCCW = Conduit(position, Direction.left45(direction))
    override def turnCW = Conduit(position, Direction.right45(direction))
}

case class Source (positionI : Point, direction : Direction.Value, color : LineColor.Value) extends Gate (positionI) {
    override def act (inRay : Ray) = (Nil, this)
    override def toXML = <Source x={position.x.toString} y={position.y.toString} direction={direction.toString} color={color.toString} />
    override def turnCCW = Source(position, Direction.left45(direction), color)
    override def turnCW = Source(position, Direction.right45(direction), color)
}


object ColorNeed extends Enumeration {
    val req = Value("required"); val met = Value("met"); val unwanted = Value("unwanted")
}

case class ColorNeeds(red : ColorNeed.Value, green : ColorNeed.Value, blue : ColorNeed.Value) {
    def redMet = ColorNeeds(ColorNeed.met, green, blue)
    def greenMet = ColorNeeds(red, ColorNeed.met, blue)
    def blueMet = ColorNeeds(red, green, ColorNeed.met)
    def allOk = (red != ColorNeed.req) && (green != ColorNeed.req) && (blue != ColorNeed.req)
}

case class Detector (positionI : Point, on : Boolean, colorNeeds : ColorNeeds, forcedOff : Boolean) extends Gate (positionI) {
    override def act (inRay : Ray) = {
        if (forcedOff) (Ray(position, inRay.d, inRay.c) :: Nil, Detector(position, false, colorNeeds, true))
        else {
            val forceOff = inRay.c match {
                case LineColor.r => colorNeeds.red == ColorNeed.unwanted
                case LineColor.g => colorNeeds.green == ColorNeed.unwanted
                case LineColor.b => colorNeeds.blue == ColorNeed.unwanted
            }
            if (forceOff)
                (Ray(position, inRay.d, inRay.c) :: Nil, Detector(position, false, colorNeeds, true))
            else {
                val newColorNeeds : ColorNeeds = inRay.c match {
                    case LineColor.r => colorNeeds.redMet
                    case LineColor.g => colorNeeds.greenMet
                    case LineColor.b => colorNeeds.blueMet
                }
                (Ray(position, inRay.d, inRay.c) :: Nil, Detector(position, newColorNeeds.allOk, newColorNeeds, false))
            }
        }
    }
    override def toString = "B(" + position + ":" + on + ")"
    override def toXML = <Detector x={position.x.toString} y={position.y.toString} red={colorNeeds.red.toString} green={colorNeeds.green.toString} blue={colorNeeds.blue.toString} />
    override def turnCW = this
    override def turnCCW = this
}

case class CrossMirror (positionI : Point, direction : Direction.Value) extends Gate (positionI) {
    def reflectRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 315 => Ray(position, Direction.left135(inRay.d), inRay.c) :: Nil
        case 270 => Ray(position, Direction.left45(inRay.d), inRay.c) :: Nil 
        case 45 => Ray(position, Direction.right45(inRay.d), inRay.c) :: Nil 
        case 0 => Ray(position, Direction.right135(inRay.d), inRay.c) :: Nil 
        case _ => Nil 
    }

    override def act(inRay : Ray) = (reflectRay(inRay), this)

    override def turnCCW = CrossMirror(position, Direction.left45(direction))
    override def turnCW = CrossMirror(position, Direction.right45(direction))

    override def toString = "M(" + position + ":" + direction + ")"
    override def toXML = <CrossMirror x={position.x.toString} y={position.y.toString} direction={direction.toString} />
}

case class Prism (positionI : Point, direction : Direction.Value) extends Gate (positionI) {
    def splitRedRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 90 => Ray(position, inRay.d, inRay.c) :: Nil 
        case 270 => Ray(position, inRay.d, inRay.c) :: Nil 
        case _ => Nil 
    }

    def splitGreenRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 45 => Ray(position, Direction.right45(inRay.d), inRay.c) :: Nil 
        case 90 => Ray(position, Direction.right45(inRay.d), inRay.c) :: Nil 
        case 270 => Ray(position, Direction.left45(inRay.d), inRay.c) :: Nil 
        case 315 => Ray(position, Direction.left45(inRay.d), inRay.c) :: Nil 
        case _ => Nil 
    }

    def splitBlueRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 90 => Ray(position, Direction.right90(inRay.d), inRay.c) :: Nil 
        case 270 => Ray(position, Direction.left90(inRay.d), inRay.c) :: Nil 
        case 0 => Ray(position, Direction.left90(inRay.d), inRay.c) :: Ray(position, Direction.right90(inRay.d), inRay.c) :: Nil 
        case _ => Nil 
    }

    override def act(inRay : Ray) = inRay.c match {
        case LineColor.r => (splitRedRay(inRay), this)
        case LineColor.g => (splitGreenRay(inRay), this)
        case LineColor.b => (splitBlueRay(inRay), this)
    }

    override def turnCCW = Prism(position, Direction.left45(direction))
    override def turnCW = Prism(position, Direction.right45(direction))

    override def toString = "Prism(" + position + ":" + direction + ")"
    override def toXML = <Prism x={position.x.toString} y={position.y.toString} direction={direction.toString} />
}

case class Mirror (positionI : Point, direction : Direction.Value) extends Gate (positionI) {
    def reflectRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 0 => Ray(position, Direction.reverse(inRay.d), inRay.c) :: Nil
        case 45 => Ray(position, Direction.right90(inRay.d), inRay.c) :: Nil 
        case 315 => Ray(position, Direction.left90(inRay.d), inRay.c) :: Nil 
        case _ => Nil 
    }

    override def act(inRay : Ray) = (reflectRay(inRay), this)

    override def turnCCW = Mirror(position, Direction.left45(direction))
    override def turnCW = Mirror(position, Direction.right45(direction))

    override def toString = "M(" + position + ":" + direction + ")"
    override def toXML = <Mirror x={position.x.toString} y={position.y.toString} direction={direction.toString} />
}

case class PartialMirror (positionI : Point, direction : Direction.Value) extends Gate (positionI) {
    def reflectRay (inRay : Ray) = Direction.angle(inRay.d, Direction.reverse(direction)) match {
        case 0 => Ray(position, Direction.reverse(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil
        case 180 => Ray(position, Direction.reverse(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil
        case 45 => Ray(position, Direction.right90(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil 
        case 135 => Ray(position, Direction.left90(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil 
        case 225 => Ray(position, Direction.right90(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil 
        case 315 =>  Ray(position, Direction.left90(inRay.d), inRay.c) :: Ray(position, inRay.d, inRay.c) :: Nil
        case _ => Nil 
    }

    override def act(inRay : Ray) = (reflectRay(inRay), this)

    override def turnCCW = PartialMirror(position, Direction.left45(direction))
    override def turnCW = PartialMirror(position, Direction.right45(direction))

    override def toString = "PM(" + position + ":" + direction + ")"
    override def toXML = <PartialMirror x={position.x.toString} y={position.y.toString} direction={direction.toString} />
}

case class Point(x:Int, y:Int) {
    def moveUp = Point(x, y - 1)
    def moveDown = Point(x, y + 1)
    def moveLeft = Point(x - 1, y)
    def moveRight = Point(x + 1, y)
}

case class Bound (topleft : Point, botright : Point) {
    def contains(p:Point) = (  (p.x >= topleft.x ) && (p.x < botright.x)
                            && (p.y >= topleft.y ) && (p.y < botright.y))

    def containsOneOff(p:Point) = (  (p.x >= topleft.x - 1 ) && (p.x <= botright.x )
                            && (p.y >= topleft.y - 1 ) && (p.y <= botright.y))

    def width = botright.x - topleft.x
    def height = botright.y - topleft.y
}

class Line(start:Point, color : LineColor.Value) {
    def points (b:Bound) : List[Point] = Nil
}

object LineColor extends Enumeration { 
    val w = Value("white"); val r = Value("red"); val g = Value("green"); val b = Value("blue"); 
}

case class Segment(start:Point, end:Point, color:LineColor.Value) extends Line(start, color) {
    override def points (b:Bound) : List[Point] = Nil
    override def toString = "Segment:" + start + " -> " + end
}

case class Ray(start:Point, d:Direction.Value, c:LineColor.Value) extends Line(start, c) {
    def nextPoint (p : Point) = d match {
        case Direction.n => Point(p.x, p.y - 1)
        case Direction.ne => Point(p.x + 1, p.y - 1)
        case Direction.e => Point(p.x + 1, p.y)
        case Direction.es => Point(p.x + 1, p.y + 1)
        case Direction.s => Point(p.x, p.y + 1)
        case Direction.sw => Point(p.x - 1, p.y + 1)
        case Direction.w => Point(p.x - 1, p.y)
        case Direction.wn => Point(p.x - 1, p.y - 1)
    }

    override def points (b:Bound) = {
        var l : List[Point] = Nil
        var point = start
        while (b.containsOneOff(point)) {
            l = point :: l
            point = nextPoint(point)
        }
        l.reverse
    }

    /** Shoots a ray, and gives a list of resulting line-segments */
    def shootRay (bound:Bound, gateList : List[Gate], visitedGates : List[(Gate,Direction.Value, LineColor.Value)]) : (List[Segment], List[Gate]) = {
        var l : List[Segment] = Nil
        var prevPoint = start
        var point = nextPoint(prevPoint)
        var done = false
        var traceSegment : List[Segment] = Nil
        var traceGate : List[Gate] = gateList

        while (!done && bound.containsOneOff(point)) {
            gateList.find(g => g.position == point) match {
                case Some(gate) => {
                    def addUniqueGate (gate : Gate, gates : List[Gate]) = gate :: gates.filterNot(g => g.position == gate.position)

                    if (!visitedGates.contains((gate, d, c))) {
                        val (rays, newgate) = gate.act(this)

                        // println (gate + " shoots " + rays + " and becomes " + newgate)
                        traceGate = addUniqueGate(newgate, traceGate)

                        val returnList = Misc.shootRayList(bound, traceGate, (gate, d, c) :: visitedGates, rays)
                        traceSegment = returnList._1
                        traceGate = returnList._2
                    }
                    prevPoint = point
                    done = true
                }
                case None => {
                    prevPoint = point
                    point = nextPoint(point)
                }
            }
        }
        val returnGates = gateList.filterNot (x => traceGate.find(y => y.position == x.position).isDefined)
        (Segment(start, prevPoint, c) :: traceSegment , traceGate ::: returnGates)
    }

    override def toString = "Ray " + start + " in " + d
}


object Direction extends Enumeration {  /*{{{*/
    val n = Value("n"); val e = Value("e"); val s = Value("s"); val w = Value("w"); 
    val ne = Value("ne"); val es = Value("es"); val sw = Value("sw"); val wn = Value("wn"); 

    def right45(d : this.Value) = d match {
        case `n` => ne;
        case `ne` => e
        case `e` => es
        case `es` => s
        case `s` => sw
        case `sw` => w
        case `w` => wn
        case `wn` => n
    }
    def left45 (d : this.Value) = d match {
        case `s` => es
        case `es` => e
        case `e` => ne
        case `ne` => n
        case `n` => wn
        case `wn` => w
        case `w` => sw
        case `sw` => s
    }
    def right90(d:Value) = right45(right45(d))
    def left90(d:Value) = left45(left45(d))
    def right135(d:Value) = right45(right90(d))
    def left135(d:Value) = left45(left90(d))
    def reverse(d:Value) = right90(right90(d))

    def angle(d1:Value) = d1 match {
        case `n` => 0; case `ne` => 45; case `e` => 90; case `es` => 135;
        case `s` => 180; case `sw` => 225; case `w` =>  270; case `wn` => 315;
    }

    def mod (x : Int, n : Int) = if (x >= 0) x % n else (x + n)

    def angle(d1:Value, d2:Value) : Int = mod(angle(d1) - angle(d2), 360)

}/*}}}*/

object Misc {
  def shootRayList (bound : Bound, gateList : List[Gate], visitedGates : List[(Gate,Direction.Value, LineColor.Value)], rays : List[Ray]) = {
      var traceSegment : List[Segment] = Nil
      var traceGate : List[Gate] = gateList

      rays.foreach (ray => {
          val (seg, listnewgate) = ray.shootRay(bound, traceGate, visitedGates)
          traceSegment = seg ::: traceSegment
          traceGate = listnewgate
      })
      (traceSegment, traceGate)
  }
}

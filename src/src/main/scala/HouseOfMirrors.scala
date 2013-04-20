package hom

import scala.xml._
object Main {
  import javax.swing.{JFileChooser, filechooser,ImageIcon}
  import javax.swing.border.EmptyBorder
  import java.io.{File, FileWriter}
  import scala.swing.{Label, Dimension}

  val statusBar = new Label("HoM") {
    preferredSize = new Dimension(250, 100)
    border = new EmptyBorder(20,10,10,10)
  }
  val statusIcon = new Label() {
    icon = new ImageIcon("gamedata/thumbsUp.png")
    border = new EmptyBorder(20,10,10,10)
  }

  object homFileFilter extends filechooser.FileFilter {
      def getExtension (f : File ) = {
          val name = f.getName
          val i = name.lastIndexOf('.')

          if (i > 0 &&  i < name.length - 1) {
              name.substring(i+1).toLowerCase();
          } else {
              ""
          }
      }

      override def accept(f : File) = {
          if (f.isDirectory) true
          else {
              getExtension(f) == "hom"
          }
      }
      override def getDescription = "House of Mirror files (*.hom)"
  }

  def openGameFile() : Unit = {
      val fc = new JFileChooser
      fc.setCurrentDirectory(new File(java.lang.System.getProperty("user.dir")))
      fc.setFileFilter (homFileFilter)

      if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          arena.fromXML (XML.loadFile(fc.getSelectedFile))
      }
  }

  def saveGameFile() : Unit = {
      val fc = new JFileChooser
      fc.setCurrentDirectory(new File(java.lang.System.getProperty("user.dir")))
      // fc.setFileSelectionMode(JFileChooser.SAVE_DIALOG)
      fc.setFileFilter (homFileFilter)

      if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          val file = fc.getSelectedFile
          file.createNewFile
          val fileWriter = new FileWriter(file)
          fileWriter.write(arena.prettyPrinter.format(arena.toXML))
          fileWriter.close
      }
  }

  val myHouse = (new scala.swing.Component {
    override lazy val peer = new HouseGui
    preferredSize = new Dimension(600,600)

    def updateArena = {
      peer.arenaModified = true
      peer.repaint(100)
    }
  })

  arena.loadPack (new File("rowhouses/rowhouse.homp"))

  val helpText = (  "<html><h1>Keyboard shortcuts</h1><br/><br/><dl>" + 
                    "<dt>n (p)</dt><dd>Select next (previous) gate<br/><br/></dd>" +
                    "<dt>SPACE (ENTER)</dt><dd>Rotate the selected element Counter-clockwise (Clockwise)<br/><br/></dd>" +
                    "<dt>UP,DOWN,LEFT,RIGHT</dt><dd>Move the selected element<br/><br/></dd>" +
                    "<dt>PAGE_UP (PAGE_DOWN)</dt><dd>Go to next (previous) level<br/><br/></dd>" +
                    "</dl></html>")

}

object HouseOfMirrors extends swing.SimpleSwingApplication {
  import scala.swing.{MainFrame,MenuBar, Menu, MenuItem, Action,Dialog,BoxPanel,Orientation}

  def top = {
    val frame = new MainFrame{
      title="House of Mirrors"
      menuBar = new MenuBar {
                  contents += (new Menu ("Game Menu") {
                    contents += (new MenuItem (Action("Open...") {
                        Main.openGameFile(); Main.myHouse.updateArena
                    }))
                    contents += (new MenuItem (Action("Save...") {
                        Main.saveGameFile(); Main.myHouse.updateArena
                    }))
                  })

                  contents += (new MenuItem (Action("Help") {
                    Dialog.showMessage(Main.myHouse, Main.helpText)
                  }))

                }
      contents = new BoxPanel (Orientation.Horizontal) {
        contents += Main.myHouse
        contents += new BoxPanel(Orientation.Vertical) {
          contents += Main.statusBar
          contents += Main.statusIcon
        }
      }
    }

    Main.myHouse.requestFocus
    frame
  }
}

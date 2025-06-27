package fun.curried.laa_editor

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import scala.util.chaining.*

object MainMenuBar extends JMenuBar:
  private val fileChooser = JFileChooser()
  fileChooser.setFileFilter(FileNameExtensionFilter("Windows Executable (*.exe, a.k.a PE Format)", "exe"))

  add:
    JMenu("File").tap: file =>
      file.setMnemonic('F')
      file.add:
        JMenuItem("Open").tap: open =>
          open.setMnemonic('O')
          open.addActionListener: e =>
            val result = fileChooser.showOpenDialog(MainFrame)
            if result == JFileChooser.APPROVE_OPTION then MainFrame.doChooseFile(fileChooser.getSelectedFile)
      file.add:
        JMenuItem("Exit").tap: exit =>
          exit.setMnemonic('X')
          exit.addActionListener: e =>
            MainFrame.doExit()
end MainMenuBar

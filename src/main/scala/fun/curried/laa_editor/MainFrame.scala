package fun.curried.laa_editor

import java.awt.FlowLayout
import java.io.File
import javax.swing.*
import scala.compiletime.uninitialized
import scala.util.chaining.*

object MainFrame extends JFrame:
  private var file: File = uninitialized

  private val fileLabel   = JLabel("Not open yet.")
  private val exeLabel    = JLabel()
  private val laaLabel    = JLabel()
  private val laaCheckBox = JCheckBox("Large Address Aware")

  // 使用系统主题
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  // 设置主窗口
  setTitle("LAA Editor")
  // 关闭窗口时退出应用
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setSize(800, 600)
  setResizable(false)
  // 将窗口置于屏幕中心
  setLocationRelativeTo(null)

  // 设置菜单栏
  setJMenuBar(MainMenuBar)

  // 设置内容区域
  getContentPane.tap: contentPane =>
    contentPane.setLayout(FlowLayout())
    contentPane.add(fileLabel)
    contentPane.add(exeLabel)
    contentPane.add(laaLabel)
    contentPane.add(laaCheckBox)

  // 设置内部组件
  this.laaCheckBox.setEnabled(false)
  this.laaCheckBox.addActionListener: e =>
    if this.file != null then
      LaaTool.setLargeAddressAware(this.file, this.laaCheckBox.isSelected)
      doChooseFile(this.file)

  def doChooseFile(file: File): Unit =
    this.file = file
    val isExe = LaaTool.isWindowsExecutable(file)
    this.fileLabel.setText(file.getPath)
    this.exeLabel.setText(s"This file is ${if isExe then "a" else "not a"} Windows Executable.")
    if isExe then
      this.laaLabel.setText(s"LargeAddressAware: ${LaaTool.getLargeAddressAware(file)}")
      this.laaCheckBox.setEnabled(true)
      this.laaCheckBox.setSelected(LaaTool.getLargeAddressAware(file))
    else
      this.laaLabel.setText("N/A")
      this.laaCheckBox.setEnabled(false)
    end if
  end doChooseFile

  def doExit(): Unit =
    dispose()

  def main(args: Array[String]): Unit =
    SwingUtilities.invokeLater: () =>
      MainFrame.setVisible(true)
end MainFrame

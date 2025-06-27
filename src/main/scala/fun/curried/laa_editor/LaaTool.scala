package fun.curried.laa_editor

import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.{Path, StandardOpenOption}
import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal

object LaaTool:
  // DOS 头, 从文件 0x00 开始
  private final val DOS_HEADER_SIZE = 64
  private final val DOS_MAGIC       = Array[Byte]('M', 'Z')
  private final val DOS_MAGIC_SIZE  = 2

  // PE 标签, 其偏移量在 DOS 头 0x3C 处定义
  private final val PE_SIGNATURE_OFFSET_IN_DOS_HEADER = 0x3c
  private final val PE_SIGNATURE                      = Array[Byte]('P', 'E', 0x00, 0x00)
  private final val PE_SIGNATURE_SIZE                 = 4

  // PE 文件头 (IMAGE_FILE_HEADER)
  private final val IMAGE_FILE_HEADER_SIZE = 20
  // Characteristics 字段在 IMAGE_FILE_HEADER 内部的偏移量
  private final val CHARACTERISTICS_OFFSET_IN_HEADER = 18

  // LargeAddressAware 标志位
  private final val LARGE_ADDRESS_AWARE_BIT = 0x0020 // 0b0000_0000_0010_0000

  private def withCharacteristics[T](path: Path)(func: (FileChannel, Int, Short) => T): T =
    val channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)
    try
      // 1. 读取并验证 DOS 头
      val dosHeader = ByteBuffer.allocate(DOS_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
      if channel.read(dosHeader) != DOS_HEADER_SIZE then
        throw new IllegalArgumentException(s"文件大小不足，无法读取完整的 DOS 头 (需要 $DOS_HEADER_SIZE 字节)。文件: $path")
      dosHeader.flip()

      if !dosHeader.array().take(DOS_MAGIC_SIZE).sameElements(DOS_MAGIC) then
        throw new IllegalArgumentException(s"不是有效的可执行文件：缺少 'MZ' DOS 头部签名。文件: $path")

      // 2. 获取 PE 头偏移量
      val peSignatureOffset = dosHeader.getInt(PE_SIGNATURE_OFFSET_IN_DOS_HEADER)
      channel.position(peSignatureOffset)

      // 3. 读取并验证 PE 签名
      val peSignature = ByteBuffer.allocate(PE_SIGNATURE_SIZE)
      if channel.read(peSignature) != PE_SIGNATURE_SIZE then
        throw new IllegalArgumentException(s"文件格式错误：无法在偏移量 $peSignatureOffset 处读取 PE 签名，文件可能已损坏。文件: $path")

      if !peSignature.array().sameElements(PE_SIGNATURE) then
        throw new IllegalArgumentException(s"不是有效的 PE 文件：在预期位置未找到 'PE\\0\\0' 签名。文件: $path")

      // 4. 定位并读取 Characteristics 字段
      val characteristicsOffset = peSignatureOffset + PE_SIGNATURE_SIZE + CHARACTERISTICS_OFFSET_IN_HEADER
      channel.position(characteristicsOffset)

      val characteristicsBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
      if channel.read(characteristicsBuffer) != 2 then
        throw new IllegalArgumentException(s"文件格式错误：无法读取 PE 文件头的 Characteristics 字段，文件可能已损坏。文件: $path")

      characteristicsBuffer.flip()

      // 5. 执行传入的操作
      func(channel, characteristicsOffset, characteristicsBuffer.getShort())
    finally channel.close()
    end try
  end withCharacteristics

  def isWindowsExecutable(file: File): Boolean =
    if !file.exists() || !file.isFile then return false

    try withCharacteristics(file.toPath)((_, _, _) => true)
    catch case NonFatal(_) => false
  end isWindowsExecutable

  def getLargeAddressAware(file: File): Boolean =
    if !file.exists() || !file.isFile then
      throw new IllegalArgumentException(s"操作失败：文件不存在或不是一个有效文件。路径: ${file.getPath}")

    withCharacteristics(file.toPath): (_, _, characteristics) =>
      (characteristics & LARGE_ADDRESS_AWARE_BIT) != 0
  end getLargeAddressAware

  def setLargeAddressAware(file: File, value: Boolean): Unit =
    if !file.exists() || !file.isFile then
      throw new IllegalArgumentException(s"操作失败：文件不存在或不是一个有效文件。路径: ${file.getPath}")

    withCharacteristics(file.toPath): (channel, characteristicsOffset, characteristics) =>
      val newCharacteristics =
        if value then (characteristics | LARGE_ADDRESS_AWARE_BIT).toShort
        else (characteristics & ~LARGE_ADDRESS_AWARE_BIT).toShort

      if newCharacteristics != characteristics then
        val bufferToWrite = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        bufferToWrite.putShort(newCharacteristics)
        bufferToWrite.flip()

        channel.position(characteristicsOffset)
        channel.write(bufferToWrite)
      end if
  end setLargeAddressAware
end LaaTool

package refuel.cipher.aes

import javax.crypto.Cipher
import refuel.cipher.algorithm.CryptType.AES
import refuel.cipher.{BytesTranscoder, CipherAlg, CryptographyConverter}
import refuel.domination.Inject
import refuel.domination.InjectionPriority.Finally
import refuel.injector.AutoInject

import scala.util.Try

@Inject(Finally)
class AESCipher(override val bytesTranscoder: BytesTranscoder, override val mode: CipherAlg[AES])
    extends CryptographyConverter[AES]
    with AutoInject {

  def encrypt(value: Array[Byte], key: AES#Key): Try[Array[Byte]] = Try {
    val cipher = mode.cipher
    cipher.init(Cipher.ENCRYPT_MODE, key.key, key.iv)
    bytesTranscoder.encodeToBytes(cipher.doFinal(value))
  }

  def decrypt(value: Array[Byte], key: AES#Key): Try[Array[Byte]] = Try {
    val cipher = mode.cipher
    cipher.init(Cipher.DECRYPT_MODE, key.key, key.iv)
    cipher.doFinal(bytesTranscoder.decodeToBytes(value))
  }
}

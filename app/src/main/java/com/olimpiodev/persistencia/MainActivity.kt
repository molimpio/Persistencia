package com.olimpiodev.persistencia

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRead.setOnClickListener {
            btnReadClick()
        }

        btnSave.setOnClickListener {
            btnSaveClick()
        }
    }

    private fun btnReadClick() {
        when (rgType.checkedRadioButtonId) {
            R.id.rbInternal -> loadFromInternal()
            R.id.rbExternalPriv -> loadFromExternal(true)
            R.id.rbExternalPublic -> loadFromExternal(false)
        }
    }

    private fun btnSaveClick() {
        when (rgType.checkedRadioButtonId) {
            R.id.rbInternal -> saveToInternal()
            R.id.rbExternalPriv -> saveToExternal(true)
            R.id.rbExternalPublic -> saveToExternal(false)
        }
    }

    private fun saveToInternal() {
        try {
            val fos = openFileOutput("arquivo.txt", Context.MODE_PRIVATE)
            save(fos)
        } catch (e : Exception) {
            Log.i("PERSISTENCIA", "Erro ao salvar o arquivo", e)
        }
    }

    private fun loadFromInternal() {
        try {
            val fis = openFileInput("arquivo.txt")
            load(fis)
        } catch (e : Exception) {
            Log.i("PERSISTENCIA", "Erro ao carregar o arquivo", e)
        }
    }

    private fun saveToExternal(privateDir: Boolean) {
        val hasPermission = checkStoragePermission(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            RC_STORAGE_PERMISSION
        )
        if (!hasPermission) {
            return
        }

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val myDir = getExternalDir(privateDir)
            try {
                if (myDir?.exists() == false) {
                    myDir.mkdir()
                }
                val txtFile = File(myDir, "arquivo.txt")
                if (!txtFile.exists()) {
                    txtFile.createNewFile()
                }
                val fos = FileOutputStream(txtFile)
                save(fos)
            } catch (e : IOException) {
                Log.i("PERSISTENCIA", "Erro ao salvar o arquivo", e)
            }
        } else {
            Log.i("PERSISTENCIA", "N??o ?? possivel escrever no SD Card")
        }
    }
    private fun loadFromExternal(privateDir: Boolean) {
        val hasPermission = checkStoragePermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            RC_STORAGE_PERMISSION
        )
        if (!hasPermission) {
            return
        }

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            val myDir = getExternalDir(privateDir)
            val txtFile = File(myDir, "arquivo.txt")

            if (txtFile.exists()) {
                try {
                    txtFile.createNewFile()
                    val fis = FileInputStream(txtFile)
                    load(fis)
                } catch (e : IOException) {
                    Log.i("PERSISTENCIA", "Erro ao carregar o arquivo", e)
                }
            }
        } else {
            Log.i("PERSISTENCIA", "SD Card indispon??vel")
        }
    }

    private fun save(fos: FileOutputStream) {
        val lines = TextUtils.split(edtText.text.toString(), "\n")
        val writer = PrintWriter(fos)
        for (line in lines) {
            writer.println(line)
        }
        writer.flush()
        writer.close()
        fos.close()
    }

    private fun load(fis: FileInputStream) {
        val reader = BufferedReader(InputStreamReader(fis))
        val sb = StringBuilder()
        do {
            val line = reader.readLine() ?: break
            if (sb.isNotEmpty()) sb.append('\n')
            sb.append(line)
        } while (true)
        reader.close()
        fis.close()
        txtText.text = sb.toString()
    }

    private fun getExternalDir(privateDir: Boolean) =
        if (privateDir) getExternalFilesDir(null)
        else Environment.getExternalStorageDirectory()

    private fun checkStoragePermission(permission: String, requestCode: Int) : Boolean {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, R.string.message_permission_request, Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_STORAGE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        val RC_STORAGE_PERMISSION = 0
    }
}
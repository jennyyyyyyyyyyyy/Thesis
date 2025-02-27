package thesis.filconnected.admin.model_version

import GestureAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R
import java.io.File

class Training : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GestureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        val btnAddLabel = findViewById<LinearLayout>(R.id.btnAddLabel)
        btnAddLabel.setOnClickListener {
            val intent = Intent(this, TrainingCamera::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val gestureFolders = getGestureFolders()
        adapter = GestureAdapter(gestureFolders) { folderName ->
            deleteFolder(folderName)
        }
        recyclerView.adapter = adapter
    }

    private fun getGestureFolders(): MutableList<String> {
        val datasetPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/gesture_dataset"
        val datasetDir = File(datasetPath)

        if (!datasetDir.exists()) {
            datasetDir.mkdirs()
        }

        return datasetDir.listFiles()?.filter { it.isDirectory }?.map { it.name }?.toMutableList() ?: mutableListOf()
    }

    private fun deleteFolder(folderName: String) {
        val folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/gesture_dataset/$folderName"
        val folderFile = File(folderPath)

        if (folderFile.exists() && folderFile.isDirectory) {
            folderFile.deleteRecursively() // Delete folder and contents
            Toast.makeText(this@Training, "File deleted.",Toast.LENGTH_SHORT).show()
            val updatedFolders = getGestureFolders()
            adapter.updateFolders(updatedFolders) // Refresh RecyclerView
        }
    }
}

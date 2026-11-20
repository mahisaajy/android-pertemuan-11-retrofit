package com.ma.androidpertemuan11


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ma.androidpertemuan11.data.api.RetrofitClient
import com.ma.androidpertemuan11.data.model.Post
import com.ma.androidpertemuan11.data.model.PostRequest
import com.ma.androidpertemuan11.databinding.ActivityPostDetailBinding
import com.ma.androidpertemuan11.databinding.DialogEditTitleBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private var postId: String = ""
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        postId = intent.getStringExtra(EXTRA_POST_ID) ?: ""
        title = "Detail Post"

        binding.btnPut.setOnClickListener { openEditForm() }
        binding.btnPatch.setOnClickListener { showPatchDialog() }
        binding.btnDelete.setOnClickListener { deletePost() }
    }

    override fun onResume() {
        super.onResume()
        loadPost()
    }

    private fun loadPost() {
        setLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                showPost(RetrofitClient.apiService.getPostById(postId))
                setLoading(isLoading = false)
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun openEditForm() {
        val post = currentPost ?: return
        val intent = Intent(this, PostFormActivity::class.java).apply {
            putExtra(PostFormActivity.EXTRA_IS_EDIT, true)
            putExtra(PostFormActivity.EXTRA_POST_ID, post.id)
        }
        startActivity(intent)
    }

    private fun showPatchDialog() {
        val dialogBinding = DialogEditTitleBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("PATCH — Ubah Judul")
            .setView(dialogBinding.root)
            .setPositiveButton("PATCH") { _, _ ->
                val newTitle = dialogBinding.etNewTitle.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "Bagian ini wajib diisi", Toast.LENGTH_SHORT).show()
                } else {
                    updateWithPatch(newTitle)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateWithPatch(newTitle: String) {
        val request = PostRequest(title = newTitle)
        setLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.patchPost(postId, request)
                response.body()?.let { showPost(it) }
                setLoading(isLoading = false)
                showResultDialog("PATCH /posts/$postId → ${response.code()}", response.body())
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun deletePost() {
        setLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost(postId)
                val rawBody = response.body()?.string() ?: "-"
                setLoading(isLoading = false)
                AlertDialog.Builder(this@PostDetailActivity)
                    .setTitle("DELETE /posts/$postId → ${response.code()}")
                    .setMessage("Post berhasil dihapus. Server mengembalikan data yang terhapus: $rawBody")
                    .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                    .show()
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun showPost(post: Post) {
        currentPost = post
        binding.tvPostLabel.text = "Post #${post.id}"
        binding.tvTitle.text = post.title
        binding.tvBody.text = post.body
    }

    private fun showResultDialog(title: String, post: Post?) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(RetrofitClient.prettyGson.toJson(post))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.content.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        setLoading(isLoading = false)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }
}

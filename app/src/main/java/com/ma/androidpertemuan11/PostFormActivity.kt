package com.ma.androidpertemuan11


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
import com.ma.androidpertemuan11.data.model.PostRequest
import com.ma.androidpertemuan11.databinding.ActivityPostFormBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostFormBinding
    private var isEdit = false
    private var postId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isEdit = intent.getBooleanExtra(EXTRA_IS_EDIT, false)
        postId = intent.getStringExtra(EXTRA_POST_ID) ?: ""

        if (isEdit) {
            title = "Edit Post"
            binding.btnSubmit.text = "PUT — Simpan Perubahan"
            loadPostForEdit()
        } else {
            title = "Tambah Post"
            binding.btnSubmit.text = "POST — Simpan Post"
        }

        binding.btnSubmit.setOnClickListener { submit() }
    }

    private fun loadPostForEdit() {
        setLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                val post = RetrofitClient.apiService.getPostById(postId)
                binding.etTitle.setText(post.title)
                binding.etBody.setText(post.body)
                setLoading(isLoading = false)
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun submit() {
        binding.etTitle.error = null
        binding.etBody.error = null

        val titleText = binding.etTitle.text.toString().trim()
        val bodyText = binding.etBody.text.toString().trim()

        var isValid = true
        if (titleText.isEmpty()) {
            binding.etTitle.error = "Bagian ini wajib diisi"
            isValid = false
        }
        if (bodyText.isEmpty()) {
            binding.etBody.error = "Bagian ini wajib diisi"
            isValid = false
        }
        if (!isValid) return

        val request = PostRequest(title = titleText, body = bodyText)
        setLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                val response = if (isEdit) {
                    RetrofitClient.apiService.updatePost(postId, request)
                } else {
                    RetrofitClient.apiService.createPost(request)
                }
                setLoading(isLoading = false)
                val method = if (isEdit) "PUT /posts/$postId" else "POST /posts"
                AlertDialog.Builder(this@PostFormActivity)
                    .setTitle("$method → ${response.code()}")
                    .setMessage(RetrofitClient.prettyGson.toJson(response.body()))
                    .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                    .show()
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        setLoading(isLoading = false)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IS_EDIT = "extra_is_edit"
        const val EXTRA_POST_ID = "extra_post_id"
    }
}

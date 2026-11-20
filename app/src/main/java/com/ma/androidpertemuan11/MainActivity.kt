package com.ma.androidpertemuan11

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ma.androidpertemuan11.data.api.RetrofitClient
import com.ma.androidpertemuan11.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val postAdapter = PostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.lvPosts.adapter = postAdapter
        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, PostFormActivity::class.java))
        }
        binding.tvError.setOnClickListener { loadPosts() }
        postAdapter.onItemClick = { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
                .putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        showLoading(isLoading = true)
        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.apiService.getPosts()
                postAdapter.updateData(posts)
                showLoading(isLoading = false)
            } catch (e: IOException) {
                showError("Gagal memuat data. Periksa koneksi internet Anda, lalu ketuk untuk mencoba lagi.")
            } catch (e: HttpException) {
                showError("Terjadi kesalahan server (kode ${e.code()}). Ketuk untuk mencoba lagi.")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.lvPosts.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        showLoading(isLoading = false)
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

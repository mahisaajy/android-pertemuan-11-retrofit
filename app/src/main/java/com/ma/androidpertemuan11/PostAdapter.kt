package com.ma.androidpertemuan11

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ma.androidpertemuan11.data.model.Post
import com.ma.androidpertemuan11.databinding.ItemPostBinding

class PostAdapter(
    private val posts: MutableList<Post> = mutableListOf()
) : BaseAdapter() {

    var onItemClick: ((Post) -> Unit)? = null

    fun updateData(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = posts.size

    override fun getItem(position: Int): Post = posts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.tag as? ItemPostBinding
            ?: ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
                it.root.tag = it
            }

        val post = getItem(position)
        binding.tvPostId.text = "Post #${post.id}"
        binding.tvTitle.text = post.title
        binding.tvBody.text = post.body
        binding.root.setOnClickListener { onItemClick?.invoke(post) }

        return binding.root
    }
}

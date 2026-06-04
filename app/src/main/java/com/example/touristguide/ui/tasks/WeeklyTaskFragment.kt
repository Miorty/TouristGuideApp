package com.example.touristguide.ui.tasks

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class WeeklyTaskFragment : Fragment(R.layout.fragment_weekly_task) {
    private val viewModel: WeeklyTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = TaskAdapter { task ->
            viewModel.addProgress(task.id)
        }
        view.findViewById<RecyclerView>(R.id.tasksRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        viewModel.weeklyTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
        }
        viewModel.taskResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.error ?: "Прогресс задания обновлен", Toast.LENGTH_SHORT).show()
        }
        viewModel.ensureTasks()
    }
}

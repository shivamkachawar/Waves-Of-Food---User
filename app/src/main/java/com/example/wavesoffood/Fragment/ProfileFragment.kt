package com.example.wavesoffood.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.wavesoffood.LoginActivity
import com.example.wavesoffood.Model.UserModel
import com.example.wavesoffood.R
import com.example.wavesoffood.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var binding : FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater , container , false)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setUserData()

        setFieldsEnabled(false)
        binding.saveInfoButton.visibility = View.GONE

        binding.editButton.setOnClickListener {
            setFieldsEnabled(true)
            binding.saveInfoButton.visibility = View.VISIBLE
        }
        binding.logOutButton.setOnClickListener{
            AlertDialog.Builder(requireContext() , R.style.AlertDialogTheme)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.saveInfoButton.setOnClickListener{
            val name = binding.nameField.text.toString()
            val email = binding.emailField.text.toString()
            val phone = binding.phoneField.text.toString()
            val address = binding.addressField.text.toString()

            updateUserData(name, email, phone, address)
        }
        return (binding.root)
    }
    private fun setFieldsEnabled(enabled: Boolean) {
        val fields = listOf(binding.nameField, binding.phoneField, binding.addressField)

        for (field in fields) {
            if (enabled) {
                // Editing mode
                field.isFocusable = true
                field.isFocusableInTouchMode = true
                field.isClickable = true
                field.background = resources.getDrawable(android.R.drawable.edit_text, null) // default editable look
            } else {
                // View mode
                field.isFocusable = false
                field.isClickable = false
                field.background = null // remove underline/grey look
            }
        }

    }
    private fun updateUserData(name: String, email: String, phone: String, address: String) {
        val userId = auth.currentUser?.uid
        if(userId != null){
            val userRef = database.getReference("user").child(userId)
            val userData = mapOf(
                "name" to name,
                "address" to address,
                "email" to email,
                "phone" to phone
            )
            userRef.updateChildren(userData).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully" , Toast.LENGTH_SHORT).show()
                setFieldsEnabled(false)
                binding.saveInfoButton.visibility = View.GONE
            }.addOnFailureListener{
                Toast.makeText(requireContext(), "Profile update failed" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if(userId != null){
            val userRef = database.getReference("user").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val userProfile : UserModel? = snapshot.getValue(UserModel::class.java)
                        if(userProfile != null){
                            binding.nameField.setText(userProfile.name)
                            binding.addressField.setText(userProfile.address)
                            binding.phoneField.setText(userProfile.phone)
                            binding.emailField.setText(userProfile.email)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }


}
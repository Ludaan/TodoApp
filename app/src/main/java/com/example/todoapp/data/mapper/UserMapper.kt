package com.example.todoapp.data.mapper

import com.example.todoapp.domain.model.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser?.toDomainUser(): User? {
    return this?.let { firebaseUser -> // 'this' se refiere al FirebaseUser que llama a la función
        User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName, // Este será el username si se actualizó
            photoUrl = firebaseUser.photoUrl?.toString(), // Convierte Uri a String si no es nulo
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }
}
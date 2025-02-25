package com.example.myapplication

import android.content.Context
import android.os.AsyncTask
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendMailTask(
    private val context: Context,
    private val email: String,
    private val subject: String,
    private val message: String
) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.example.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("levalladares@uce.edu.ec", "YEYVh118")
            }
        })

        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress("levalladares@uce.edu.ec"))
                addRecipient(Message.RecipientType.TO, InternetAddress(email))
                setSubject(subject)
                setText(message)
            }

            Transport.send(mimeMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}
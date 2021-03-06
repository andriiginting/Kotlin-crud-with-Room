package com.rahmat.app.samplecrudkotlin.features.detail

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.rahmat.app.samplecrudkotlin.R
import com.rahmat.app.samplecrudkotlin.db.StudentDatabase
import com.rahmat.app.samplecrudkotlin.entity.Student
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class DetailActivity : AppCompatActivity() {

    lateinit var student:Student

    var studentDatabase:StudentDatabase? = null
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        student = intent.getParcelableExtra("student_object")

        textViewName.text = "Nama = ${student.name}"
        textViewNim.text = "Nim = ${student.nim}"
        textViewGender.text = "Jenis Kelamin = ${student.gender}"

        studentDatabase = StudentDatabase.getInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.delete -> {
                deleteStudent(student)
                Toast.makeText(this, "Data  ${student.name} berhasil dihapus", Toast.LENGTH_LONG).show()
                finish()
            }
            R.id.edit -> {
                val dialogBuilder = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.input_dialog, null)
                dialogBuilder.setView(view)
                dialogBuilder.setTitle("Masukkan data baru")
                val et_name = view.ed_student_name
                val et_nim = view.ed_student_id
                val radioGroupGender = view.radio_group_gender
                dialogBuilder.setPositiveButton("Tambahkan") { _: DialogInterface, _: Int ->
                    val studentName = et_name.text
                    val studentNim = et_nim.text
                    var gender: String
                    val selectedRadioButton = radioGroupGender.checkedRadioButtonId
                    when (selectedRadioButton) {
                        R.id.radio_female -> gender = "Perempuan"
                        else -> gender = "Laki-laki"
                    }
//                    updateStudent(student.id, studentName.toString(), studentNim.toString(), gender))
                    updateStudent(student.id, studentName.toString(), studentNim.toString(), gender)
                    Toast.makeText(this, "Data berhasil diubah $studentName ", Toast.LENGTH_LONG).show()
                    textViewName.text = "Nama = ${studentName}"
                    textViewNim.text = "Nim = ${studentNim}"
                    textViewGender.text = "Jenis Kelamin = ${gender}"
                }
                dialogBuilder.setNegativeButton("Batal") { _: DialogInterface, _: Int ->
                }
                dialogBuilder.show()
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
    fun deleteStudent(student:Student){
        compositeDisposable.add(Observable.fromCallable{studentDatabase?.studentDao()?.delete(student)}
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe())
    }
    fun updateStudent(studentId: Long, studentName:String, studentNim:String, studentGen:String){
        compositeDisposable.add(Observable.fromCallable { studentDatabase?.studentDao()?.update(studentId,
                studentName, studentNim, studentGen) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe())
    }
    override fun onDestroy() {
        super.onDestroy()
        StudentDatabase.destroyInstance()
        compositeDisposable.dispose()
    }
}

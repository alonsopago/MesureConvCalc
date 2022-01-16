package com.example.mesureconvcalc

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils.replace

import android.text.TextWatcher
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.core.view.size
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {
    //Array of types and subtypes measures
    val arrMeasures: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    //Array of convertion factors
    var arrFactors: ArrayList<String> = ArrayList<String>()
    var arrSubClassFactors: ArrayList<String> = ArrayList<String>()

    // *** UI components
    //Spinners, adapters and positions.
    lateinit var spnMeasureClass: Spinner
    var posMeasureClass: Int = 0
    lateinit var spnMeasureSubClassFrom: Spinner
    private lateinit var adtMeasureSubClassFrom: ArrayAdapter<String>
    var posMeasureSubClassFrom: Int = 0
    lateinit var spnMeasureSubClassTo: Spinner
    private lateinit var adtMeasureSubClassTo: ArrayAdapter<String>
    var posMeasureSubClassTo: Int = 0

    //Measures from and to convert
    lateinit var etMeasureSubClassFrom: EditText
    lateinit var tvMeasureSubClassTo: TextView

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         //Load information array from resource
        val workArray = resources.getStringArray(R.array.arrMeasures)

         // Split loaded information into measures class and subclass names array
         // and  convertion factors array
         for(i in (0..workArray.size-1)) {
             if(i.mod(2) == 0){
                 // measures class and subclass names array
                 arrMeasures.add(workArray[i].split(",").map { it -> it.trim() } as java.util.ArrayList<String>)
             }
             else{
                 // convertion factors array
                 arrFactors.add(workArray[i])
             }
         }
         Log.d("Array measures:", arrMeasures.toString())
         // Build Measures Class List
         var tempArrayMut: MutableList<String> = emptyList<String>().toMutableList()
         for(element in arrMeasures) {
             if(element.size > 1) { //Only include defined measures
                 tempArrayMut.add(element[0])
             }
         }

        // Build measure factors convertions array
         //arrSubClassFactors = arrFactors.get(0).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
         arrSubClassFactors = arrFactors.get(0).split(",").map { it -> it.trim() } as java.util.ArrayList<String>

         // Set spinnerClass list
         spnMeasureClass = findViewById(R.id.spnMeasureClass)
         // Create an ArrayAdapter using the string array and a default spinner layout
         val adapter = ArrayAdapter(
             this,
             android.R.layout.simple_spinner_item,
             tempArrayMut
         )/*.also { adapter ->
             // Specify the layout to use when the list of choices appears
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
             // Apply the adapter to the spinner
             spinner.adapter = adapter
         }*/
         spnMeasureClass.adapter = adapter

         // Set spinnerClass on click
         spnMeasureClass.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {

                 //Costruct subtype list
                 // Aquí voy...
                 posMeasureClass = position
                 tempArrayMut = arrMeasures.get(posMeasureClass).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
                 tempArrayMut.removeAt(0)
                 val temp: String = tempArrayMut[tempArrayMut.size-1]
                 tempArrayMut[tempArrayMut.size-1] = temp.dropLast(1)
                 Log.d("Ult elemen array", tempArrayMut[tempArrayMut.size-1])
                 arrSubClassFactors = arrFactors.get(posMeasureClass).split(",").map { it -> it.trim() } as java.util.ArrayList<String>

                 // Populate adtMeasureSubClassFrom with new list
                 adtMeasureSubClassFrom.clear()
                 adtMeasureSubClassFrom.addAll(tempArrayMut)
                 adtMeasureSubClassFrom.notifyDataSetChanged()
                 spnMeasureSubClassFrom.setSelection(0)
                 posMeasureSubClassFrom = 0

                 // Populate adtMeasureSubClassTo with new list
                 adtMeasureSubClassTo.clear()
                 adtMeasureSubClassTo.addAll(tempArrayMut)
                 adtMeasureSubClassTo.notifyDataSetChanged()
                 spnMeasureSubClassTo.setSelection(0)
                 posMeasureSubClassTo = 0
                 calcConversion(posMeasureSubClassFrom, posMeasureSubClassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Build Measures Subtype List
         tempArrayMut = arrMeasures.get(posMeasureClass).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
         tempArrayMut.removeAt(0)
         // Set spinnerFrom subclass list for from measure convert
         spnMeasureSubClassFrom = findViewById(R.id.spnMeasureSubClassFrom)
         // Create an ArrayAdapter using the string array and a default spinner layout
         adtMeasureSubClassFrom = ArrayAdapter(
             this,
             android.R.layout.simple_spinner_item,
             tempArrayMut
         )
         spnMeasureSubClassFrom.adapter = adtMeasureSubClassFrom

         //Set spinnerFrom onClick
         spnMeasureSubClassFrom.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {
                 posMeasureSubClassFrom = position
                 calcConversion(posMeasureSubClassFrom, posMeasureSubClassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Set spinner list for to measure convert
         spnMeasureSubClassTo = findViewById(R.id.spnMeasureSubClassTo)
         // Create an ArrayAdapter using the string array and a default spinner layout
         adtMeasureSubClassTo = ArrayAdapter(
             this,
             android.R.layout.simple_spinner_item,
             tempArrayMut
         )
         spnMeasureSubClassTo.adapter = adtMeasureSubClassTo

         //Set spinnerTo on click
         spnMeasureSubClassTo.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {
                 posMeasureSubClassTo = position
                 calcConversion(posMeasureSubClassFrom, posMeasureSubClassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Input of quantity
         etMeasureSubClassFrom = findViewById<EditText>(R.id.etMeasureSubClassFrom)
         tvMeasureSubClassTo = findViewById<EditText>(R.id.tvMeasureSubClassTo)
         etMeasureSubClassFrom.setFilters(arrayOf<InputFilter>(InputFilterMinMax(0F, 9999999.99F, 2)))
         etMeasureSubClassFrom.addTextChangedListener(object : TextWatcher {
             override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                 Log.d("Before------->", s.toString() + " - " +  s.length + " - " + start + " - " + count + " - " + after )
             }
             override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                 Log.d("On------->", s.toString() + " - " +  s.length + " - " + start + " - " + count)
                 calcConversion(posMeasureSubClassFrom, posMeasureSubClassTo)
                 /*
                 if(!s.isEmpty()) {
                     calcConversion(posMeasureSubClassFrom, posMeasureSubClassTo)
                 }
                 else{
                     tvMeasureSubClassTo.setText("0.0")
                     //etMeasureSubClassFrom.setText("0.0")

                 }
                 if(!s.isDigitsOnly()){
                     Toast.makeText(this@MainActivity,"Please, enter a valid quantity...", Toast.LENGTH_SHORT)
                     etMeasureSubClassFrom.setText("")
                     tvMeasureSubClassTo.setText("0.0")
                 }
                  */
             }

             override fun afterTextChanged(s: Editable) {
                 Log.d("After------->", s.toString())
             }
         })

         val button: ImageButton = findViewById(R.id.information)
         button.setOnClickListener {
             //Toast.makeText(this, getString(R.string.ConverHelp), Toast.LENGTH_LONG).show()// Code here executes on main thread after user presses button
         }
     }

    // Calc the conversion
    fun calcConversion(fromIndex: Int, toIndex: Int){
        /*
        Log.d("*********",
            arrFactors[posMeasureClass])
        Log.d("???????????", fromIndex.toString() + " - " +
                toIndex.toString() + " - " + arrSubClassFactors.size + " - " + adtMeasureSubClassFrom.count)
        for(i in (0..arrSubClassFactors.size-1)){
            Log.d("###########",i.toString() + " -> " + arrSubClassFactors[i])
        }
        Log.d("Index: ", (((fromIndex*spnMeasureSubClassFrom.size)+1)+toIndex).toString()
        )
        */
        //var measure: String = etMeasureSubClassFrom.text.toString()
        //measure.replace("\\s".toRegex(), "")
        //etMeasureSubClassFrom.setText("$measure")
        if(!etMeasureSubClassFrom.text.isEmpty()){
            //val credits = dec.format(number)
            //vValueInterest.text = credits
            Log.d("Cantidad a convertir:", etMeasureSubClassFrom.text.toString().toFloat().toString())
            Log.d("Factor: ", arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex])
            Log.d("Array:", arrSubClassFactors.toString())
            val dec = DecimalFormat("#,##0.00#############")
            val temp: String = arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].substring(0, 1)
            Log.d("Operation, zzzzzzzzzzz", temp)
            when(temp) {
                //if(arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].startsWith("*")){
                "*" -> {
                    Log.d(
                        "Multiplicar por:",
                        arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "*",
                            ""
                        ).toFloat().toString()
                    )
                    Log.d(
                        "Resultado:",
                        (etMeasureSubClassFrom.text.toString()
                            .toFloat() * arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "*",
                            ""
                        ).toFloat()).toBigDecimal().toPlainString()
                    )
                    //                tvMeasureSubClassTo.setText((etMeasureSubClassFrom.text.toString().toFloat() * arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("*","").toFloat()).toBigDecimal().toPlainString())
                    //                val result = dec.format((etMeasureSubClassFrom.text.toString().toFloat() * arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("*","").toFloat()).toBigDecimal())
                    //                val result =   dec.format(etMeasureSubClassFrom.text.toString().toFloat() * arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("*","").toFloat()).toString()
                    val result = (etMeasureSubClassFrom.text.toString()
                        .toFloat() * arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                        "*",
                        ""
                    ).toFloat()).toString()
                    tvMeasureSubClassTo.setText(result)
                    //tvMeasureSubClassTo.setText("1234567890123456789.000123456")
                    Log.d("Resultado formatead", result)
                }
                "/" -> {
                    Log.d(
                        "Dividir por:",
                        arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "/",
                            ""
                        ).toFloat().toString()
                    )
                    Log.d(
                        "Resultado:",
                        (etMeasureSubClassFrom.text.toString()
                            .toFloat() / arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "/",
                            ""
                        ).toFloat()).toBigDecimal().toPlainString()
                    )
                    //tvMeasureSubClassTo.setText((etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal().toPlainString())
//                val result = dec.format((etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal())
//                val result = dec.format(etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toString()
                    val result = (etMeasureSubClassFrom.text.toString()
                        .toFloat() / arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                        "/",
                        ""
                    ).toFloat()).toString()
                    tvMeasureSubClassTo.setText(result)
                    //tvMeasureSubClassTo.setText("1234567890123456789.000123456")
                    Log.d("Resultado formatead", result)
                }
                "-" -> {
                    Log.d(
                        "Dividir por:",
                        arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "-",
                            ""
                        ).toFloat().toString()
                    )
                    Log.d(
                        "Resultado:",
                        (arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                            "-",
                            ""
                        ).toFloat() / etMeasureSubClassFrom.text.toString()
                            .toFloat() ).toBigDecimal().toPlainString()
                    )
                    //tvMeasureSubClassTo.setText((etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal().toPlainString())
//                val result = dec.format((etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal())
//                val result = dec.format(etMeasureSubClassFrom.text.toString().toFloat() / arrSubClassFactors[fromIndex*(adtMeasureSubClassFrom.count)+toIndex].replaceFirst("/","").toFloat()).toString()
                    val result = (arrSubClassFactors[fromIndex * (adtMeasureSubClassFrom.count) + toIndex].replaceFirst(
                        "-",
                        ""
                    ).toFloat() / etMeasureSubClassFrom.text.toString().toFloat()).toString()
                    tvMeasureSubClassTo.setText(result)
                    //tvMeasureSubClassTo.setText("1234567890123456789.000123456")
                    Log.d("Resultado formatead", result)
                }
            }
            Log.d("NOEmpty", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
        }
        else{
            //etMeasureSubClassFrom.setText("0.0")
            Log.d("Empty", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        }
    }
}

class InputFilterMinMax(min:Float, max:Float, decimals: Int): InputFilter {
    private var min:Float = 0.0F
    private var max:Float = 0.0F
    private var decimals = 0

    init{
        this.min = min
        this.max = max
        this.decimals = decimals

    }

    override fun filter(source:CharSequence, start:Int, end:Int, dest: Spanned, dstart:Int, dend:Int): CharSequence? {
        /*
        Log.d("------ Source", source.toString())
        Log.d("------ Start", start.toString())
        Log.d("------ End", end.toString())
        Log.d("------ Dest", dest.toString())
        Log.d("------ Index of .", (dest.toString().indexOf(".") + 1).toString())
        Log.d("------ Lenght", dest.toString().length.toString())
        Log.d("------ Lenght", dest.toString().length.toString())
        Log.d("------ Dstart", dstart.toString())
        Log.d("------ Dend", dend.toString())
         */
        try
        {
            val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toFloat()
            Log.d("Input", input.toString())
            //isInDecimals(input, 2, etMeasureSubClassFrom)
            if (isInRange(min, max, input) && isInDecimals(input, decimals))
                return null
        }
        catch (nfe:NumberFormatException) {}
        return ""
    }

    private fun isInRange(a:Float, b:Float, c:Float):Boolean {
        return if (b > a) c in a..b else c in b..a
    }

    // Filter for edit text not editable
    private fun isInDecimals(c:Float, d: Int):Boolean {
        if((c.toString().length - (c.toString().indexOf(".") + 1)) <= decimals) {
            val numDec = c.toString().length - (c.toString().indexOf(".") + 1)
            Log.d("Longitud", c.toString().length.toString())
            Log.d("index .", (c.toString().indexOf(".")+1).toString())
            Log.d("Cifras decimales", numDec.toString())
            Log.d("c",c.toString())
            return true
        }
        return false
    }
}
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
    val measuresTypeSubArray: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    //Array of convertion factors
    var measureFactorArray: ArrayList<String> = ArrayList<String>()
    var subClassFactorArray: ArrayList<String> = ArrayList<String>()
    //Adapters for spinners From and To
    private lateinit var adapterFrom: ArrayAdapter<String>
    private lateinit var adapterTo: ArrayAdapter<String>
    //Spinners
    lateinit var subMeasureTypeFromSpn: Spinner
    lateinit var subMeasureTypeToSpn: Spinner
    lateinit var spnMesClass: Spinner
    lateinit var subMeasureTypeFromEt: EditText
    lateinit var subMeasureToTv: TextView
    //Spinners positions
    var posMesClass: Int = 0
    var posMesSubclassFrom: Int = 0
    var posMesSubclassTo: Int = 0
    //value to convert
    //var convertQuantity:Float = 0F

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         //Load information array from resource
        var workArray = resources.getStringArray(R.array.measures_array)
        // Split loaded information
         for(i in (0..workArray.size-1)) {
             if(i.mod(2) == 0){
                 // pull apart meassures categories and names
                 measuresTypeSubArray.add(workArray[i].split(",").map { it -> it.trim() } as java.util.ArrayList<String>)
             }
             else{
                 // pull apart factors for measures conversions
                 measureFactorArray.add(workArray[i])
             }
         }

         // Build Measures type List
         var tempArrayMut: MutableList<String> = emptyList<String>().toMutableList()
         var i: Int
         for(element in measuresTypeSubArray) {
             i = 0
             if(element.size > 1) { //Only include defined measures
                 for (element1 in element) {
                     if (i == 0) {
                         tempArrayMut.add(element1)
                         i = 1
                     }
                 }
             }
         }

         subClassFactorArray = measureFactorArray.get(0).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>

         // Set spinnerClass list
         spnMesClass = findViewById(R.id.measureTypeSpn)
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
         spnMesClass.adapter = adapter

         // Set spinnerClass on click
         spnMesClass.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {

                 //Costruct subtype list
                 posMesClass = position
                 tempArrayMut = measuresTypeSubArray.get(posMesClass).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
                 subClassFactorArray = measureFactorArray.get(posMesClass).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
                 tempArrayMut.removeAt(0)

                 // Populate adapterFrom with new list
                 adapterFrom.clear()
                 adapterFrom.addAll(tempArrayMut)
                 adapterFrom.notifyDataSetChanged()
                 subMeasureTypeFromSpn.setSelection(0)
                 posMesSubclassFrom = 0

                 // Populate adapterTo with new list
                 adapterTo.clear()
                 adapterTo.addAll(tempArrayMut)
                 adapterTo.notifyDataSetChanged()
                 subMeasureTypeToSpn.setSelection(0)
                 posMesSubclassTo = 0
                 calcConversion(posMesSubclassFrom, posMesSubclassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Build Measures Subtype List
         tempArrayMut = measuresTypeSubArray.get(posMesClass).toString().split(",").map { it -> it.trim() } as java.util.ArrayList<String>
         tempArrayMut.removeAt(0)
         // Set spinnerFrom subclass list for from measure convert
         subMeasureTypeFromSpn = findViewById(R.id.subMeasureTypeFromSpn)
         // Create an ArrayAdapter using the string array and a default spinner layout
         adapterFrom = ArrayAdapter(
             this,
             android.R.layout.simple_spinner_item,
             tempArrayMut
         )
         subMeasureTypeFromSpn.adapter = adapterFrom

         //Set spinnerFrom onClick
         subMeasureTypeFromSpn.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {
                 posMesSubclassFrom = position
                 calcConversion(posMesSubclassFrom, posMesSubclassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Set spinner list for to measure convert
         subMeasureTypeToSpn = findViewById(R.id.subMeasureTypeToSpn)
         // Create an ArrayAdapter using the string array and a default spinner layout
         adapterTo = ArrayAdapter(
             this,
             android.R.layout.simple_spinner_item,
             tempArrayMut
         )
         subMeasureTypeToSpn.adapter = adapterTo

         //Set spinnerTo on click
         subMeasureTypeToSpn.onItemSelectedListener = object :
             AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>,
                                         view: View, position: Int, id: Long) {
                 posMesSubclassTo = position
                 calcConversion(posMesSubclassFrom, posMesSubclassTo)
             }

             override fun onNothingSelected(parent: AdapterView<*>) {
                 // write code to perform some action
             }
         }

         // Input of quantity
         subMeasureTypeFromEt = findViewById<EditText>(R.id.subMeasureTypeFromEt)
         subMeasureTypeFromEt.setText("0.0")
         subMeasureToTv = findViewById<EditText>(R.id.subMeasureToTv)
         subMeasureToTv.setText("0.0")
         subMeasureToTv.setOnFocusChangeListener { _, hasFocus ->
             if (hasFocus) {
                 Log.d("tomo focus", "focus .....")
                 Toast.makeText(this,"Use arrows to scrroll the the result.", Toast.LENGTH_SHORT)
             } else {
                 // Nothing
             }
         }
         subMeasureTypeFromEt.setFilters(arrayOf<InputFilter>(InputFilterMinMax(0F, 9999999.99F, 2)))
         subMeasureTypeFromEt.addTextChangedListener(object : TextWatcher {
             override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                 Log.d("Before------->", s.toString() + " - " +  s.length + " - " + start + " - " + count + " - " + after )
             }
             override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                 Log.d("On------->", s.toString() + " - " +  s.length + " - " + start + " - " + count)
                 calcConversion(posMesSubclassFrom, posMesSubclassTo)
                 /*
                 if(!s.isEmpty()) {
                     calcConversion(posMesSubclassFrom, posMesSubclassTo)
                 }
                 else{
                     subMeasureToTv.setText("0.0")
                     //subMeasureTypeFromEt.setText("0.0")

                 }
                 if(!s.isDigitsOnly()){
                     Toast.makeText(this@MainActivity,"Please, enter a valid quantity...", Toast.LENGTH_SHORT)
                     subMeasureTypeFromEt.setText("")
                     subMeasureToTv.setText("0.0")
                 }
                  */
             }

             override fun afterTextChanged(s: Editable) {
                 Log.d("After------->", s.toString())
             }
         })

     }

    // Calc the conversion
    fun calcConversion(fromIndex: Int, toIndex: Int){
        /*
        Log.d("*********",
            measureFactorArray[posMesClass])
        Log.d("???????????", fromIndex.toString() + " - " +
                toIndex.toString() + " - " + subClassFactorArray.size + " - " + adapterFrom.count)
        for(i in (0..subClassFactorArray.size-1)){
            Log.d("###########",i.toString() + " -> " + subClassFactorArray[i])
        }
        Log.d("Index: ", (((fromIndex*subMeasureTypeFromSpn.size)+1)+toIndex).toString()
        )
        */
        //var measure: String = subMeasureTypeFromEt.text.toString()
        //measure.replace("\\s".toRegex(), "")
        //subMeasureTypeFromEt.setText("$measure")
        if(!subMeasureTypeFromEt.text.isEmpty()){
            //val credits = dec.format(number)
            //vValueInterest.text = credits
            Log.d("Cantidad a convertir:", subMeasureTypeFromEt.text.toString().toFloat().toString())
            Log.d("Factor: ", subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex])
            Log.d("Array:", subClassFactorArray.toString())
            val dec = DecimalFormat("#,##0.00#############")
            if(subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].startsWith("*")){
                Log.d("Multiplicar por:", subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("*","").toFloat().toString())
                Log.d("Resultado:", (subMeasureTypeFromEt.text.toString().toFloat() * subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("*","").toFloat()).toBigDecimal().toPlainString())
//                subMeasureToTv.setText((subMeasureTypeFromEt.text.toString().toFloat() * subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("*","").toFloat()).toBigDecimal().toPlainString())
                val result = dec.format((subMeasureTypeFromEt.text.toString().toFloat() * subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("*","").toFloat()).toBigDecimal())
                subMeasureToTv.setText(result)
                Log.d("Resultado formatead", result)
            }
            else{
                Log.d("Dividir por:", subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("/","").toFloat().toString())
                Log.d("Resultado:", (subMeasureTypeFromEt.text.toString().toFloat() / subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal().toPlainString())
                //subMeasureToTv.setText((subMeasureTypeFromEt.text.toString().toFloat() / subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal().toPlainString())
                val result = dec.format((subMeasureTypeFromEt.text.toString().toFloat() / subClassFactorArray[fromIndex*(adapterFrom.count)+toIndex].replaceFirst("/","").toFloat()).toBigDecimal())
                subMeasureToTv.setText(result)
                Log.d("Resultado formatead", result)
            }
            Log.d("NOEmpty", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
        }
        else{
            //subMeasureTypeFromEt.setText("0.0")
            Log.d("Empty", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        }
    }
}

class InputFilterMinMax(min:Float, max:Float, decimals: Int): InputFilter {
    private var min:Float = 0.0F
    private var max:Float = 0.0F
    private var decimals =0

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
            //isInDecimals(input, 2, subMeasureTypeFromEt)
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
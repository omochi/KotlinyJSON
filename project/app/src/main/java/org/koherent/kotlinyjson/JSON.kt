package org.koherent.kotlinyjson

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap

public class JSON {
    private var jsonObject: JSONObject?
    private var jsonArray: JSONArray?

    private var parent: JSON?
    private var name: String?
    private var index: Int?

    private var value: Any?

    init {
        jsonObject = null
        jsonArray = null

        parent = null
        name = null
        index = null

        value = null
    }

    public constructor(data: ByteArray) {
        val string = String(data, Charsets.UTF_8)

        try {
            jsonObject = JSONObject(string)
        } catch (e: JSONException) {
            try {
                jsonArray = JSONArray(string)
            } catch (e2: JSONException) {
                try {
                    val parent = JSON("[$string]".toByteArray(Charsets.UTF_8))
                    if (parent.getJSONArray()?.length() == 1) {
                        this.parent = parent
                        index = 0
                    }
                } catch (e3: JSONException) {
                }
            }
        }
    }

    public constructor(value: Boolean) {
        this.value = value
    }

    public constructor(value: Int) {
        this.value = value
    }

    public constructor(value: Long) {
        this.value = value
    }

    public constructor(value: Double) {
        this.value = value
    }

    public constructor(value: String) {
        this.value = value
    }

    public constructor(value: List<JSON>) {
        this.value = value
    }

    public constructor(value: Map<String, JSON>) {
        this.value = value
    }

    private constructor(parent: JSON, name: String) {
        this.parent = parent
        this.name = name
    }

    private constructor(parent: JSON, index: Int) {
        this.parent = parent
        this.index = index
    }

    public fun get(name: String): JSON {
        return JSON(this, name)
    }

    public fun get(index: Int): JSON {
        return JSON(this, index)
    }

    private fun <T> getValue(fromParentObject: (JSONObject, String) -> T?, fromParentArray: (JSONArray, Int) -> T?): T? {
        try {
            if (name is String) {
                val jsonObject = parent?.getJSONObject()
                if (jsonObject is JSONObject) {
                    return fromParentObject(jsonObject, name!!)
                }
            } else if (index is Int) {
                val jsonArray = parent?.getJSONArray()
                if (jsonArray is JSONArray) {
                    return fromParentArray(jsonArray, index!!)
                }
            }
        } catch(e: JSONException) {
        }

        return null
    }

    private fun getJSONObject(): JSONObject? {
        if (jsonObject !is JSONObject) {
            jsonObject = getValue({ o, n -> o.getJSONObject(n) }, { a, i -> a.getJSONObject(i) })
        }

        return jsonObject
    }

    private fun getJSONArray(): JSONArray? {
        if (jsonArray !is JSONArray) {
            jsonArray = getValue({ o, n -> o.getJSONArray(n) }, { a, i -> a.getJSONArray(i) })
        }

        return jsonArray
    }

    public val boolean: Boolean?
        get() {
            if (value == null) {
                value = getValue({ o, n -> o.getBoolean(n) }, { a, i -> a.getBoolean(i) })
            }
            return value as? Boolean
        }

    public val int: Int?
        get() {
            if (value == null) {
                value = getValue({ o, n -> o.getInt(n) }, { a, i -> a.getInt(i) })
            }
            return value as? Int
        }

    public val long: Long?
        get() {
            if (value == null) {
                value = getValue({ o, n -> o.getLong(n) }, { a, i -> a.getLong(i) })
            }
            return value as? Long
        }

    public val double: Double?
        get() {
            if (value == null) {
                value = getValue({ o, n -> o.getDouble(n) }, { a, i -> a.getDouble(i) })
            }
            return value as? Double
        }

    public val string: String?
        get() {
            if (value == null) {
                value = getValue({ o, n -> o.getString(n) }, { a, i -> a.getString(i) })
            }
            return value as? String
        }

    public val list: List<JSON>?
        get() {
            if (value == null) {
                val length =getJSONArray()?.length()
                if (length is Int) {
                    val result = ArrayList<JSON>()
                    for (index in 0..(length - 1)) {
                        result.add(JSON(this, index))
                    }
                    value = result
                }
            }

            return value as? List<JSON>
        }

    public val map: Map<String, JSON>?
        get() {
            if (value == null) {
                val names = getJSONObject()?.keys()
                if (names is Iterator<String>) {
                    val result = HashMap<String, JSON>()
                    while (names.hasNext()) {
                        val name = names.next()
                        result.put(name, this[name])
                    }
                    value = result
                }
            }

            return value as? Map<String, JSON>
        }

    public fun rawString(): String? {
        val jsonObject = getJSONObject()
        if (jsonObject != null) {
            return jsonObject.toString()
        }

        val jsonArray = getJSONArray()
        if (jsonArray != null) {
            return jsonArray.toString()
        }

        val booleanValue = boolean
        if (booleanValue != null) {
            return booleanValue.toString()
        }

        val doubleValue = double
        if (doubleValue != null) {
            if (doubleValue.toLong().toDouble() != doubleValue) { // has decimals
                return doubleValue.toString()
            }
        }

        val longValue = long // int is included
        if (longValue != null) {
            return longValue.toString()
        }

        val stringValue = string
        if (stringValue != null) {
            return JSONObject.quote(stringValue)
        }

        return null
    }

    override public fun toString(): String {
        return rawString() ?: ""
    }
}

public val JSON.booleanValue: Boolean
    get() = boolean!!

public val JSON.intValue: Int
    get() = int!!

public val JSON.longValue: Long
    get() = long!!

public val JSON.doubleValue: Double
    get() = double!!

public val JSON.stringValue: String
    get() = string!!

public val JSON.listValue: List<JSON>
    get() = list!!

public val JSON.mapValue: Map<String, JSON>
    get() = map!!

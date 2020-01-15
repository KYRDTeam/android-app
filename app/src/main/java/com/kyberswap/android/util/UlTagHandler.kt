package com.kyberswap.android.util

import android.text.Editable
import android.text.Html
import org.xml.sax.XMLReader

class UlTagHandler : Html.TagHandler {
    override fun handleTag(
        opening: Boolean, tag: String, output: Editable,
        xmlReader: XMLReader
    ) {
        if (tag == "li" && opening) output.append("\t•")
        if (tag == "li" && !opening) output.append("\n")
    }
}
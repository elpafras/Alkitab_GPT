package org.sabda.gpt.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import org.sabda.gpt.R

class CustomExpandableListAdapter(
    var context: Context,
    private var listDataHeader: List<String>,
    private var listDataChild: Map<String, List<String>>
) :
    BaseExpandableListAdapter() {
    override fun getGroupCount(): Int = listDataHeader.size

    override fun getChildrenCount(groupPosition: Int): Int =
        listDataChild[listDataHeader[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = listDataHeader[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        listDataChild[listDataHeader[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val headerTitle = getGroup(groupPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_group, parent, false)

        val listHeader = view.findViewById<TextView>(R.id.listTitle)
        listHeader.setTypeface(null, Typeface.BOLD)
        listHeader.text = headerTitle

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val childText = getChild(groupPosition, childPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val expandedListItem = view.findViewById<TextView>(R.id.expadedListItem)
        expandedListItem.text = childText

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
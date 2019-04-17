package com.kyberswap.android.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.kyberswap.android.data.api.home.entity.ArticleEntity

data class Article(
    val answerCount: String = "",
    val contributor: Contributor = Contributor(),
    val likeCount: Int = 0,
    val metaTags: List<MetaTag>? = listOf(),
    val publishedAt: String = "",
    val thumbnailImageUrl: String = "",
    val title: String = "",
    val url: String = ""
) : QuoteArticle {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Contributor::class.java.classLoader),
        parcel.readInt(),
        parcel.createTypedArrayList(MetaTag),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    constructor(entity: ArticleEntity) : this(
        entity.answerCount,
        Contributor(entity.contributor),
        entity.likeCount,
        entity.metaTags?.map { it ->
            MetaTag(it!!)
,
        entity.publishedAt,
        entity.thumbnailImageUrl,
        entity.title,
        entity.url
    )

    fun preSlashJobTitle(): String {
        return if (!contributor.jobTitle.isEmpty()) " / " + contributor.jobTitle
        else contributor.jobTitle
    }

    fun preSlashAffiliation(): String {
        return if (!contributor.affiliation.isEmpty()) " / " + contributor.affiliation
        else contributor.affiliation
    }

    override fun getArticleIconUrlQuote() = thumbnailImageUrl

    override fun getAuthorIconUrlQuote() = contributor.imageUrl

    override fun getAuthorNameQuote() = contributor.name

    override fun getIdQuote() = contributor.id

    override fun getLikeCountQuote() = likeCount

    override fun getTitleQuote() = title

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(answerCount)
        parcel.writeParcelable(contributor, flags)
        parcel.writeInt(likeCount)
        parcel.writeTypedList(metaTags)
        parcel.writeString(publishedAt)
        parcel.writeString(thumbnailImageUrl)
        parcel.writeString(title)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)


        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)

    }
}
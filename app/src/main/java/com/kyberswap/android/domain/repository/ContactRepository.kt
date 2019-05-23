package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import io.reactivex.Completable
import io.reactivex.Flowable

interface ContactRepository {

    fun getContacts(param: GetContactUseCase.Param): Flowable<List<Contact>>

    fun saveContact(param: SaveContactUseCase.Param): Completable
}
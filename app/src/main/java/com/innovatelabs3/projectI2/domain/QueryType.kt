package com.innovatelabs3.projectI2.domain

sealed class QueryType {
    object ShowToast : QueryType()
    object ShowSnackbar : QueryType()
    object ShowNotification : QueryType()
    object OpenWhatsApp : QueryType()
    object SendWhatsAppMessage : QueryType()
    object Identity : QueryType()
    object General : QueryType()
    object ShowDirections : QueryType()
    object SearchYouTube : QueryType()
    object OpenInstagramProfile : QueryType()
    object JoinGoogleMeet : QueryType()
    object SearchSpotify : QueryType()
    object BookUber : QueryType()
    object SearchProduct : QueryType()
    object SaveContact : QueryType()
    object SearchFiles : QueryType()
    object SendEmail : QueryType()
    object MakeCall : QueryType()
    object OpenLinkedInProfile : QueryType()
}
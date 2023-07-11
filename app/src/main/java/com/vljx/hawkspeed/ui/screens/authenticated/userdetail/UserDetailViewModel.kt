package com.vljx.hawkspeed.ui.screens.authenticated.userdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import com.vljx.hawkspeed.domain.usecase.user.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserUseCase: GetUserUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * Get the User's UID in a mutable state flow.
     */
    private val mutableSelectedUserUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * Flat map the latest emissions from the selected User's UID to a flow for a resource containing the User.
     */
    private val userResource: Flow<Resource<User>> =
        mutableSelectedUserUid.flatMapLatest { userUid ->
            getUserUseCase(
                RequestGetUser(userUid)
            )
        }

    /**
     * Map the User's resource to the most applicable state for the User's detail.
     */
    val userDetailUiState: StateFlow<UserDetailUiState> =
        userResource.map { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> UserDetailUiState.GotUser(resource.data!!)
                Resource.Status.LOADING -> UserDetailUiState.Loading
                Resource.Status.ERROR -> UserDetailUiState.Failed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserDetailUiState.Loading)

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}
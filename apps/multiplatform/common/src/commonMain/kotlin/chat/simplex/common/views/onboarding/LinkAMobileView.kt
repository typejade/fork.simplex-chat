package chat.simplex.common.views.onboarding

import SectionTextFooter
import SectionView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import chat.simplex.common.model.ChatModel
import chat.simplex.common.platform.chatModel
import chat.simplex.common.ui.theme.DEFAULT_PADDING
import chat.simplex.common.views.helpers.*
import chat.simplex.common.views.remote.AddingMobileDevice
import chat.simplex.common.views.remote.DeviceNameField
import chat.simplex.common.views.usersettings.PreferenceToggle
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun LinkAMobile() {
  val connecting = rememberSaveable { mutableStateOf(false) }
  val deviceName = chatModel.controller.appPrefs.deviceNameForRemoteAccess
  var deviceNameInQrCode by remember { mutableStateOf(chatModel.controller.appPrefs.deviceNameForRemoteAccess.get()) }
  val staleQrCode = remember { mutableStateOf(false) }

  LinkAMobileLayout(
    deviceName = remember { deviceName.state },
    connecting,
    staleQrCode,
    updateDeviceName = {
      withBGApi {
        if (it != "" && it != deviceName.get()) {
          chatModel.controller.setLocalDeviceName(it)
          deviceName.set(it)
          staleQrCode.value = deviceName.get() != deviceNameInQrCode
        }
      }
    }
  )
  KeyChangeEffect(staleQrCode.value) {
    if (!staleQrCode.value) {
      deviceNameInQrCode = deviceName.get()
    }
  }
}

@Composable
private fun LinkAMobileLayout(
  deviceName: State<String?>,
  connecting: MutableState<Boolean>,
  staleQrCode: MutableState<Boolean>,
  updateDeviceName: (String) -> Unit,
) {
  Column(Modifier.padding(top = 20.dp)) {
    AppBarTitle(stringResource(if (remember { chatModel.remoteHosts }.isEmpty()) MR.strings.link_a_mobile else MR.strings.linked_mobiles))
    Row(Modifier.weight(1f).padding(horizontal = DEFAULT_PADDING * 2), verticalAlignment = Alignment.CenterVertically) {
      Column(
        Modifier.weight(0.3f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SectionView(generalGetString(MR.strings.this_device_name).uppercase()) {
          DeviceNameField(deviceName.value ?: "") { updateDeviceName(it) }
          SectionTextFooter(generalGetString(MR.strings.this_device_name_shared_with_mobile))
          PreferenceToggle(stringResource(MR.strings.multicast_discoverable_via_local_network), remember { ChatModel.controller.appPrefs.offerRemoteMulticast.state }.value) {
            ChatModel.controller.appPrefs.offerRemoteMulticast.set(it)
          }
        }
      }
      Box(Modifier.weight(0.7f)) {
        AddingMobileDevice(false, staleQrCode, connecting) {
          if (chatModel.remoteHosts.isEmpty()) {
            chatModel.controller.appPrefs.onboardingStage.set(OnboardingStage.Step1_SimpleXInfo)
          } else {
            chatModel.controller.appPrefs.onboardingStage.set(OnboardingStage.OnboardingComplete)
          }
        }
      }
    }
    SimpleButtonDecorated(
      text = stringResource(MR.strings.about_simplex),
      icon = painterResource(MR.images.ic_arrow_back_ios_new),
      textDecoration = TextDecoration.None,
      fontWeight = FontWeight.Medium
    ) { chatModel.controller.appPrefs.onboardingStage.set(OnboardingStage.Step1_SimpleXInfo) }
  }
}

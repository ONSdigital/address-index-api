import com.google.inject.AbstractModule
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[AddressIndexClient]).to(classOf[AddressIndexClientInstance])
  }
}

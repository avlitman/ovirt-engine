package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName"), @CustomLogField("InterfaceName") })
public class UpdateVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {
    public UpdateVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    @Override
    protected void executeCommand() {
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription()).toString());
        getVmNetworkInterfaceDao().update(getParameters().getInterface());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getInterface().getVmId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_VM_CANNOT_BE_SET);
            return false;
        }

        List<VmNetworkInterface> interfaces =
                getVmNetworkInterfaceDao().getAllForTemplate(getParameters().getVmTemplateId());

        if (getVmTemplate() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }

        // Interface oldIface = interfaces.First(i => i.id ==
        // AddVmInterfaceParameters.Interface.id);
        VmNetworkInterface oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });


        String clusterCompatibilityVersion = getVdsGroup().getcompatibility_version().getValue();
        VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion);

        if (!validate(nicValidator.linkedCorrectly()) || !validate(nicValidator.networkNameValid())) {
            return false;
        }

        if (!StringHelper.EqOp(oldIface.getName(), getParameters().getInterface().getName())) {
            if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                            getParameters().getInterface().getName(),
                            getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters()
    {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getParameters().getInterface() != null && StringUtils.isNotEmpty(getNetworkName())
                && getVmTemplate() != null) {

            VmNetworkInterface iface = getVmNetworkInterfaceDao().get(getParameters().getInterface().getId());
            if (iface != null) {
                Network network =
                        getNetworkDAO().getByNameAndCluster(getNetworkName(), getVmTemplate().getvds_group_id());

                if (getParameters().getInterface().isPortMirroring()
                        && (isNetworkChanged(iface) || !iface.isPortMirroring())) {
                    permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                            VdcObjectType.Network,
                            ActionGroup.PORT_MIRRORING));
                } else {
                    // If the vNic's network is changed, the user should have permission for using the new network
                    if (isNetworkChanged(iface)) {
                        permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                                VdcObjectType.Network,
                                getActionType().getActionGroup()));
                    }
                }
            }
        }
        return permissionList;
    }

    private boolean isNetworkChanged(VmNetworkInterface iface) {
        return !getNetworkName().equals(iface.getNetworkName());
    }

    private String getNetworkName() {
        return getParameters().getInterface().getNetworkName();
    }
}

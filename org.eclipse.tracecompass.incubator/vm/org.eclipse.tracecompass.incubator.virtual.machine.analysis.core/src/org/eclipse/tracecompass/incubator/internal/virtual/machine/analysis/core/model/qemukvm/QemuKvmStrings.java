/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.qemukvm;

/**
 * Lttng specific strings for the events used by the Qemu/KVM virtual machine
 * model
 *
 * TODO: The whole model should be updated to use the linux event layout. These
 * event names are LTTng-specific
 *
 * @author Mohamad Gebai
 */
@SuppressWarnings({ "nls" })
public interface QemuKvmStrings {

    /* vmsync events */

    /**
     * Event produced by the host, for a message sent from the guest, received
     * by the host
     */
    String VMSYNC_GH_HOST = "vmsync_gh_host";
    /**
     * Event produced by the host, for a message sent from the host, received by
     * the guest
     */
    String VMSYNC_HG_HOST = "vmsync_hg_host";
    /**
     * Event produced by the guest, for a message sent from the guest, received
     * by the host
     */
    String VMSYNC_GH_GUEST = "vmsync_gh_guest";
    /**
     * Event produced by the guest, for a message sent from the host, received
     * by the guest
     */
    String VMSYNC_HG_GUEST = "vmsync_hg_guest";
    /**
     * Event field of previous events, containing a message counter, updated at
     * each message
     */
    String COUNTER_PAYLOAD = "cnt";
    /**
     * Event field of previous events, with a unique UID to identify a single
     * guest on a host
     */
    String VM_UID_PAYLOAD = "vm_uid";

    /* kvm entry/exit events */
    /**
     * KVM kernel event indicating that virtual machine code is being run
     */
    String KVM_ENTRY = "kvm_entry";
    /**
     * Same as above but for versions of lttng >= 2.8
     */
    String KVM_X86_ENTRY = "kvm_x86_entry";
    /**
     * KVM kernel event indicating that virtual machine code is not run anymore,
     * but rather hypervisor-specific code
     */
    String KVM_EXIT = "kvm_exit";
    /**
     * Same as above but for versions of lttng >= 2.8
     */
    String KVM_X86_EXIT = "kvm_x86_exit";
    /**
     * Field from kvm_entry event indicating which virtual CPU is being run
     */
    String VCPU_ID = "vcpu_id";
    /**
     * Event that can indicate a future entry from L0 to a nested VM
     */
    String KVM_MMU_GET_PAGE = "kvm_mmu_get_page";
    /**
     * Event that tells that the next entries are not for the VM that exited
     */
    String KVM_NESTED_VMEXIT_INJECT = "kvm_nested_vmexit_inject";
}

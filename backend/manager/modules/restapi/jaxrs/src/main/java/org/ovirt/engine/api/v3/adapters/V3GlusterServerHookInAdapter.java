/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterServerHook;
import org.ovirt.engine.api.model.HookContentType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterServerHook;

public class V3GlusterServerHookInAdapter implements V3Adapter<V3GlusterServerHook, GlusterServerHook> {
    @Override
    public GlusterServerHook adapt(V3GlusterServerHook from) {
        GlusterServerHook to = new GlusterServerHook();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetChecksum()) {
            to.setChecksum(from.getChecksum());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetContentType()) {
            to.setContentType(HookContentType.fromValue(from.getContentType()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatus()) {
            to.setStatus(adaptIn(from.getStatus()));
        }
        return to;
    }
}

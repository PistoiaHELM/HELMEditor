/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.helm.editor.mapping;

import y.base.Node;

import java.util.*;

import org.helm.editor.data.ChemSequenceHolder;
import org.helm.editor.data.NodeSequence;

/**
 * User: dzhelezov
 */
public class MappedChemSequenceHolder implements ChemSequenceHolder {
    private GraphMapper mapper;
    private ChemSequenceHolder sourceHolder;

    public MappedChemSequenceHolder(GraphMapper mapper, ChemSequenceHolder sourceHolder) {
        this.mapper = mapper;
        this.sourceHolder = sourceHolder;
    }

    public List<Node> getPendantNodes() {
        return getTargetList(sourceHolder.getPendantNodes());
    }

    public Set<NodeSequence> getSequences() {
        return getTargetSet(sourceHolder.getSequences());
    }

    public List<NodeSequence> getConnectedSequences(Node dockNode) {
        Set<NodeSequence> result = new HashSet<NodeSequence>();
        for (Node sourceDockNode : mapper.getSourceNodes(dockNode)) {
            result.addAll(getTargetList(sourceHolder.getConnectedSequences(sourceDockNode)));
        }
        return Arrays.asList(result.toArray(new NodeSequence[result.size()]));
    }

    public List<Node> getDockedNodes() {
        return getTargetList(sourceHolder.getDockedNodes());
    }

    public Set<NodeSequence> getDockedSequences() {
        return getTargetSet(sourceHolder.getDockedSequences());
    }

    private <T> List<T> getTargetList(List<T> sourceList) {
        List<T> result = new ArrayList<T>();
        for (T source : sourceList) {
            result.add(getTargetInstance(source));
        }
        return result;
    }

    private <T> Set<T> getTargetSet(Set<T> sourceSet) {
        Set<T> result = new LinkedHashSet<T>();
        for (T source : sourceSet) {
            result.add(getTargetInstance(source));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T getTargetInstance(T source) {
        if (source instanceof NodeSequence) {
            return (T)(new MappedNodeSequence(mapper, (NodeSequence)source));
        } else if (source instanceof Node) {
            return (T)(mapper.getTargetNode((Node)source));
        }
        throw new RuntimeException();
    }


}

/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.workflow.action;

import de.rub.nds.tlsattacker.core.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.core.protocol.handler.ProtocolMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.IOException;

/**
 * Apply buffered message to the given context.
 * 
 * Call adjustContext() for each message in the context. Does not remove the
 * messages from buffer after execution.
 */
public class ApplyBufferedMessagesAction extends ConnectionBoundAction {

    public ApplyBufferedMessagesAction() {
    }

    public ApplyBufferedMessagesAction(String connectionAlias) {
        super(connectionAlias);
    }

    @Override
    public void execute(State state) throws WorkflowExecutionException, IOException {
        TlsContext ctx = state.getTlsContext(connectionAlias);

        if (isExecuted()) {
            throw new WorkflowExecutionException("Action already executed!");
        }
        for (ProtocolMessage msg : ctx.getMessageBuffer()) {
            LOGGER.debug("Applying buffered " + msg.toCompactString() + " to context " + ctx);
            ProtocolMessageHandler h = msg.getHandler(ctx);
            h.adjustTLSContext(msg);
        }
        setExecuted(true);
    }

    @Override
    public boolean executedAsPlanned() {
        return isExecuted();
    }

    @Override
    public void reset() {
        setExecuted(false);
    }
}

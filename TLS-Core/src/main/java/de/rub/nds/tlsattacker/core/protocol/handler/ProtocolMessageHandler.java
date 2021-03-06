/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.exceptions.AdjustmentException;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.parser.Parser;
import de.rub.nds.tlsattacker.core.protocol.parser.ProtocolMessageParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.Preparator;
import de.rub.nds.tlsattacker.core.protocol.preparator.ProtocolMessagePreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.ProtocolMessageSerializer;
import de.rub.nds.tlsattacker.core.protocol.serializer.Serializer;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @param <Message>
 *            The ProtocolMessage that should be handled
 */
public abstract class ProtocolMessageHandler<Message extends ProtocolMessage> extends Handler<Message> {

    protected static final Logger LOGGER = LogManager.getLogger(ProtocolMessageHandler.class.getName());

    /**
     * tls context
     */
    protected final TlsContext tlsContext;

    /**
     * @param tlsContext
     *            The Context which should be Adjusted with this Handler
     */
    public ProtocolMessageHandler(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
    }

    /**
     * Prepare message for sending. This method invokes before and after method
     * hooks.
     *
     * @param message
     *            The Message that should be prepared
     * @return message in bytes
     */
    public byte[] prepareMessage(Message message) {
        return prepareMessage(message, true);
    }

    /**
     * Prepare message for sending. This method invokes before and after method
     * hooks.
     *
     * @param message
     *            The message that should be prepared
     * @param withPrepare
     *            if the prepare function should be called or only the rest
     * @return message in bytes
     */
    public byte[] prepareMessage(Message message, boolean withPrepare) {
        if (withPrepare) {
            Preparator preparator = getPreparator(message);
            preparator.prepare();
            preparator.afterPrepare();
            Serializer serializer = getSerializer(message);
            byte[] completeMessage = serializer.serialize();
            message.setCompleteResultingMessage(completeMessage);
            if (message instanceof HandshakeMessage) {
                if (((HandshakeMessage) message).getIncludeInDigest()) {
                    tlsContext.getDigest().append(message.getCompleteResultingMessage().getValue());
                }
            }
        }
        try {
            if (message.getAdjustContext()) {
                adjustTLSContext(message);
            } else {
                LOGGER.debug("Not adjusting TLSContext for " + message.toCompactString());
            }
        } catch (AdjustmentException E) {
            LOGGER.warn("Could not adjust TLSContext");
            LOGGER.debug(E);
        }
        return message.getCompleteResultingMessage().getValue();
    }

    /**
     * Parses a byteArray from a Position into a MessageObject and returns the
     * parsed MessageObjet and parser position in a parser result. The current
     * Chooser is adjusted as
     *
     * @param message
     *            The byte[] messages which should be parsed
     * @param pointer
     *            The pointer (startposition) into the message bytes
     * @return The Parser result
     */
    public ParserResult parseMessage(byte[] message, int pointer) {
        Parser<Message> parser = getParser(message, pointer);
        Message parsedMessage = parser.parse();
        if (parsedMessage instanceof HandshakeMessage) {
            if (((HandshakeMessage) parsedMessage).getIncludeInDigest()) {
                tlsContext.getDigest().append(parsedMessage.getCompleteResultingMessage().getValue());
            }
        }
        try {
            prepareAfterParse(parsedMessage);
            adjustTLSContext(parsedMessage);
        } catch (AdjustmentException | UnsupportedOperationException E) {
            LOGGER.warn("Could not adjust TLSContext");
            LOGGER.debug(E);
        }

        return new ParserResult(parsedMessage, parser.getPointer());
    }

    @Override
    public abstract ProtocolMessageParser getParser(byte[] message, int pointer);

    @Override
    public abstract ProtocolMessagePreparator getPreparator(Message message);

    @Override
    public abstract ProtocolMessageSerializer getSerializer(Message message);

    /**
     * Adjusts the TLS Context according to the received or sending
     * ProtocolMessage
     *
     * @param message
     *            The Message for which this context should be adjusted
     */
    public abstract void adjustTLSContext(Message message);

    public void adjustTlsContextAfterSerialize(Message message) {
    }

    public void prepareAfterParse(Message message) {
        ProtocolMessagePreparator prep = getPreparator(message);
        prep.prepareAfterParse(tlsContext.isReversePrepareAfterParse());
    }

    @Override
    protected final void adjustContext(Message message) {
        adjustTLSContext(message);
    }
}

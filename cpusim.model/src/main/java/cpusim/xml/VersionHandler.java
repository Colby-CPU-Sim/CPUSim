package cpusim.xml;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import cpusim.model.Field;
import cpusim.model.iochannel.StreamChannel;
import cpusim.model.microinstruction.IO;
import cpusim.model.microinstruction.IODirection;
import cpusim.model.microinstruction.Shift;
import cpusim.model.module.Register;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs conversions from Strings specified within the XML file to values.
 * <br/>
 * To create instances, use {@link #forVersion(int)} to create instances.
 */
abstract class VersionHandler {
    /**
     * Version number associated with legacy XML data, this turns on old versioning.
     */
    public static final int LEGACY_VERSION_NUMBER = 0;

    /**
     * Defines how enum sets are separated
     */
    public static final char ENUM_SET_SEPARATOR = '|';

    /**
     * The version number of the machine being read, {@value LEGACY_VERSION_NUMBER} is legacy before we introduced
     * versioning.
     */
    final int versionNumber;

    final UUID consoleChannelUUID;

    private VersionHandler(int versionNumber, UUID consoleChannelUUID) {
        this.versionNumber = versionNumber;
        this.consoleChannelUUID = consoleChannelUUID;
    }

    /**
     * Factory method to create {@link VersionHandler} instances based on a version number
     * @param versionNumber Version of the xml file
     * @return Non-{@code null} {@code VersionHandler}
     */
    public static VersionHandler forVersion(final int versionNumber, UUID consoleChannelUUID) {
        if (versionNumber <= LEGACY_VERSION_NUMBER) {
            return new Version_Legacy(versionNumber, consoleChannelUUID);
        } else {
            return new Version_5(versionNumber, consoleChannelUUID);
        }
    }

    /**
     * Converts old id values to a {@link UUID} if necessary.
     * @param id Old string ID (or valid UUID per {@link UUID#fromString(String)}).
     * @return Valid UUID value.
     */
    abstract UUID getUUID(String id);

    abstract Field.Type getFieldType(String input);

    abstract Field.Relativity getFieldRelativity(String input);

    abstract Field.SignedType getFieldSigned(String input);

    abstract EnumSet<Register.Access> getMemoryAccess(String input);

    abstract IODirection getIODirection(String ioStr);

    abstract IO.Type getIOType(String typeString);

    abstract UUID getChannelId(String channelID);

    abstract Shift.ShiftDirection getShiftDirection(String directionString);

    abstract Shift.ShiftType getShiftType(String typeString);

    // Arithmetic name changes:
    abstract String getArithmeticSource1Attribute();
    abstract String getArithmeticSource2Attribute();

    /**
     * Legacy conversions
     */
    private static class Version_Legacy extends VersionHandler {

        /**
         * Legacy mapping from old IDs to UUID maps
         */
        private final Map<String, UUID> legacyIdToUUIDMap;

        Version_Legacy(int versionNumber, UUID consoleChannelUUID) {
            super(versionNumber, consoleChannelUUID);

            legacyIdToUUIDMap = new HashMap<>();
            legacyIdToUUIDMap.put("[console]", consoleChannelUUID);
        }

        @Override
        UUID getUUID(String id)  {
            if (Strings.isNullOrEmpty(id)) {
                return null;
            }

            return legacyIdToUUIDMap.computeIfAbsent(id, _key -> UUID.randomUUID());
        }

        @Override
        Field.Type getFieldType(String input) {
            String check = checkNotNull(input).trim();

            return Field.Type.valueOf(check);
        }

        @Override
        Field.Relativity getFieldRelativity(String input) {
            String check = checkNotNull(input).trim();
            return Field.Relativity.valueOf(check);
        }

        @Override
        Field.SignedType getFieldSigned(String input) {
            String check = checkNotNull(input).trim();
            if (versionNumber <= LEGACY_VERSION_NUMBER) {
                return Field.SignedType.fromBool("true".equals(check));
            } else {
                return Field.SignedType.valueOf(check);
            }
        }

        @Override
        EnumSet<Register.Access> getMemoryAccess(String input) {
            String check = checkNotNull(input).trim();

            boolean readOnly = check.equals("true");

            return (readOnly ? Register.Access.readOnly() : Register.Access.readWrite());
        }

        @Override
        IODirection getIODirection(String ioStr) {
            final String fixed = checkNotNull(ioStr).toLowerCase().trim();

            switch (fixed) {
                case "input":
                case "read":
                    return IODirection.Read;

                case "output":
                case "write":
                    return IODirection.Write;

                default:
                    throw new IllegalStateException("Unknown IO direction: " + ioStr);
            }
        }

        @Override
        IO.Type getIOType(String typeString) {
            final String fixed = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, checkNotNull(typeString).trim());

            // TODO Did we miss types in the Enum?

            return IO.Type.valueOf(fixed);
        }


        @Override
        UUID getChannelId(String channelID) {

            final String lcase = channelID == null ? "[console]" : channelID.toLowerCase().trim();
            switch (lcase) {
                case "":
                case "[console]":
                    return getUUID("[console]");
                case "[user]":
                case "[dialog]":
                    // FIXME Uh? #94 Unsure how to handle DialogChannel which is only within the Gui for obvious reasons
                    throw new IllegalStateException("Can not handle Dialog Channels currently.");
                default:
                    return getUUID(channelID);
            }
        }

        @Override
        Shift.ShiftDirection getShiftDirection(String directionString) {
            final String check = checkNotNull(directionString).toLowerCase().trim();
            return Shift.ShiftDirection.valueOf(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, check));
        }

        @Override
        Shift.ShiftType getShiftType(String typeString) {
            final String check = checkNotNull(typeString).toLowerCase().trim();
            return Shift.ShiftType.valueOf(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, check));
        }

//        @Override
//        String getArithmeticSource1Attribute() {
//            return "lhs";
//        }
//
//        @Override
//        String getArithmeticSource2Attribute() {
//            return "rhs";
//        }

        @Override
        String getArithmeticSource1Attribute() {
            return "source1";
        }

        @Override
        String getArithmeticSource2Attribute() {
            return "source2";
        }
    }

    /**
     * Conversion for CPUSim V5
     */
    private static class Version_5 extends Version_Legacy {

        Version_5(int versionNumber, UUID consoleChannelUUID) {
            super(versionNumber, consoleChannelUUID);
        }

        @Override
        Field.SignedType getFieldSigned(String input) {
            String check = checkNotNull(input).trim();
            return Field.SignedType.valueOf(check);
        }

        @Override
        EnumSet<Register.Access> getMemoryAccess(String input) {
            String check = checkNotNull(input).trim();

            // breakdown the attribute
            List<String> enumStrings = Splitter.on(ENUM_SET_SEPARATOR)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(check);

            EnumSet<Register.Access> outAccess = EnumSet.noneOf(Register.Access.class);
            enumStrings.forEach(str -> outAccess.add(Register.Access.valueOf(str)));

            return outAccess;
        }

        @Override
        IODirection getIODirection(String ioStr) {
            final String fixed = checkNotNull(ioStr).toLowerCase().trim();
            return IODirection.valueOf(fixed);
        }

        @Override
        UUID getChannelId(String channelID) {
            return getUUID(checkNotNull(channelID).trim());
        }

        @Override
        Shift.ShiftDirection getShiftDirection(String directionString) {
            return Shift.ShiftDirection.valueOf(checkNotNull(directionString).toLowerCase().trim());
        }

        @Override
        Shift.ShiftType getShiftType(String typeString) {
            final String check = checkNotNull(typeString).toLowerCase().trim();
            return Shift.ShiftType.valueOf(check);
        }

        @Override
        String getArithmeticSource1Attribute() {
            return "source1";
        }

        @Override
        String getArithmeticSource2Attribute() {
            return "source2";
        }
    }
}

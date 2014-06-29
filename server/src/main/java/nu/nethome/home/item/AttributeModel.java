package nu.nethome.home.item;

import java.util.List;

/**
 *
 */
public interface AttributeModel {
    String getName();

    List<String> getValueList();

    String getType();

    String getUnit();

    boolean isReadOnly();

    boolean isCanInit();

    boolean isWriteOnly();
}

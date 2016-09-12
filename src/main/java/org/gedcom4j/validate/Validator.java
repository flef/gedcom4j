/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.gedcom4j.validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gedcom4j.Options;
import org.gedcom4j.model.Gedcom;
import org.gedcom4j.model.ModelElement;

/**
 * <p>
 * Validates {@link Gedcom} objects.
 * </p>
 * <p>
 * Does a deep traversal over the items in the {@link Gedcom} structure and checks them for problems, errors, etc, which are
 * represented as {@link Finding} objects. These objects contain problem codes, descriptions, severity ratings, and references to
 * the objects that have problems.
 * <p>
 * <p>
 * Typical usage is to instantiate a Validator with the Gedcom being validated, call the validate() method, then call the
 * getResults()
 * </p>
 * 
 * @author frizbog
 * @since 4.0.0
 */
@SuppressWarnings("PMD.GodClass")
public class Validator implements Serializable {

    /**
     * Represents something of interest found by the validation module.
     * 
     * @author frizbog
     */
    public class Finding implements Serializable {
        /**
         * Serial Version UID
         */
        private static final long serialVersionUID = 2148459753130687833L;

        /**
         * The field name in {@link #itemOfConcern} that had the finding. Optional. If populated, reflection can be used on the
         * {@link #itemOfConcern} object to get specific field values.
         */
        private String fieldNameOfConcern;

        /**
         * The primary item that had the finding in it
         */
        private ModelElement itemOfConcern;

        /**
         * The code for the problem
         */
        private int problemCode;

        /**
         * The description of the problem
         */
        private String problemDescription;

        /**
         * Items that are related to the item of concern that are contributing to or are somehow related to the problem.
         */
        private List<ModelElement> relatedItems = (Options.isCollectionInitializationEnabled() ? new ArrayList<ModelElement>(0)
                : null);

        /**
         * The repairs made automatically by the validator
         */
        private List<AutoRepair> repairs = (Options.isCollectionInitializationEnabled() ? new ArrayList<AutoRepair>(0) : null);

        /** The severity. */
        private Severity severity;

        /**
         * Default constructor
         */
        Finding() {
            // Default constructor does nothing
        }

        /**
         * Convenience method to add an auto-repair to the finding
         * 
         * @param autoRepair
         *            the auto-repair object to add
         */
        public void addRepair(AutoRepair autoRepair) {
            repairs.add(autoRepair);
        }

        /**
         * Get the fieldNameOfConcern
         * 
         * @return the fieldNameOfConcern
         */
        public String getFieldNameOfConcern() {
            return fieldNameOfConcern;
        }

        /**
         * Get the itemOfConcern
         * 
         * @return the itemOfConcern
         */
        public ModelElement getItemOfConcern() {
            return itemOfConcern;
        }

        /**
         * Get the problemCode
         * 
         * @return the problemCode
         */
        public int getProblemCode() {
            return problemCode;
        }

        /**
         * Get the problemDescription
         * 
         * @return the problemDescription
         */
        public String getProblemDescription() {
            return problemDescription;
        }

        /**
         * Get the relatedItems
         * 
         * @return the relatedItems
         */
        public List<ModelElement> getRelatedItems() {
            return relatedItems;
        }

        /**
         * Get the relatedItems
         * 
         * @param initializeIfNeeded
         *            initialize the collection if needed prior to returning the value
         * 
         * @return the relatedItems
         */
        public List<ModelElement> getRelatedItems(boolean initializeIfNeeded) {
            if (initializeIfNeeded && relatedItems == null) {
                relatedItems = new ArrayList<>();
            }
            return relatedItems;
        }

        /**
         * Get the repairs
         * 
         * @return the repairs
         */
        public List<AutoRepair> getRepairs() {
            return repairs;
        }

        /**
         * Get the repairs
         * 
         * @param initializeIfNeeded
         *            initialize the collection if needed before returning
         * 
         * @return the repairs
         */
        public List<AutoRepair> getRepairs(boolean initializeIfNeeded) {
            if (initializeIfNeeded && repairs == null) {
                repairs = new ArrayList<>(0);
            }
            return repairs;
        }

        /**
         * Get the severity
         * 
         * @return the severity
         */
        public Severity getSeverity() {
            return severity;
        }

        /**
         * Set the user-defined problem code
         * 
         * @param problemCode
         *            the problem code to set. Must be 1000 or higher. Values below 1000 are reserved for gedcom4j. Values of 1000
         *            or higher are for user-defined validator problems.
         */
        public void setProblemCode(int problemCode) {
            if (problemCode < 0) {
                throw new IllegalArgumentException("Problem code must be a positive integer - received " + problemCode);
            }
            if (problemCode < 1000) {
                throw new IllegalArgumentException("Values under 1000 are reserved for gedcom4j - received " + problemCode);
            }
            this.problemCode = problemCode;
        }

        /**
         * Set the user-defined problem description.
         * 
         * @param problemDescription
         *            the user-defined problemDescription to set
         */
        public void setProblemDescription(String problemDescription) {
            if (problemCode < 1000) {
                throw new IllegalArgumentException(
                        "Cannot set descriptions for problems with codes under 1000, which are reserved for gedcom4j");
            }
            this.problemDescription = problemDescription;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(50);
            builder.append("Finding [");
            if (fieldNameOfConcern != null) {
                builder.append("fieldNameOfConcern=");
                builder.append(fieldNameOfConcern);
                builder.append(", ");
            }
            if (itemOfConcern != null) {
                builder.append("itemOfConcern=");
                builder.append(itemOfConcern.getClass().getName());
                builder.append(", ");
            }
            if (severity != null) {
                builder.append("severity=");
                builder.append(severity);
                builder.append(", ");
            }
            builder.append("problemCode=");
            builder.append(problemCode);
            builder.append(", ");
            if (problemDescription != null) {
                builder.append("problemDescription=");
                builder.append(problemDescription);
                builder.append(", ");
            }
            if (relatedItems != null) {
                builder.append("relatedItems=");
                builder.append(relatedItems);
                builder.append(", ");
            }
            if (repairs != null) {
                builder.append("repairs=");
                builder.append(repairs);
            }
            builder.append("]");
            return builder.toString();
        }

        /**
         * Set the fieldNameOfConcern. Deliberately package-private. Outside the validation framework, this field should not be
         * changeable.
         * 
         * @param fieldNameOfConcern
         *            the fieldNameOfConcern to set
         */
        void setFieldNameOfConcern(String fieldNameOfConcern) {
            this.fieldNameOfConcern = fieldNameOfConcern;
        }

        /**
         * Set the item of concern. Deliberately package-private. Outside the validation framework, this field should not be
         * changeable.
         * 
         * @param itemOfConcern
         *            the item of concern
         */
        void setItemOfConcern(ModelElement itemOfConcern) {
            this.itemOfConcern = itemOfConcern;
        }

        /**
         * Set a problem code and description from within gedcom4j. Deliberately package-private to prevent usage outside the
         * gedcom4j framework.
         * 
         * @param pc
         *            the problem code enum entry to use for the problem code and description
         */
        void setProblem(ProblemCode pc) {
            problemCode = pc.getCode();
            problemDescription = pc.getDescription();
        }

        /**
         * Set the related items. Deliberately package-private. Outside the validation framework, this field should not be
         * changeable.
         * 
         * @param relatedItems
         *            the related items to set
         */
        void setRelatedItems(List<ModelElement> relatedItems) {
            this.relatedItems = relatedItems;
        }

        /**
         * Set the repairs. Deliberately package-private. Outside the validation framework, this field should not be changeable.
         * 
         * @param repairs
         *            the collection of repairs
         */
        void setRepairs(List<AutoRepair> repairs) {
            this.repairs = repairs;
        }

        /**
         * Set the severity. Deliberately package-private. Outside the validation framework, this field should not be changeable.
         * 
         * @param severity
         *            the severity to set. Required.
         * @throws IllegalArgumentException
         *             if severity passed in is null
         */
        void setSeverity(Severity severity) {
            if (severity == null) {
                throw new IllegalArgumentException("severity is a required argument.");
            }
            this.severity = severity;
        }

    }

    /**
     * Built-in {@link AutoRepairResponder} implementation that allows everything to be auto-repaired.
     */
    public static final AutoRepairResponder AUTO_REPAIR_ALL = new AutoRepairResponder() {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean mayRepair(Finding repairableValidationFinding) {
            return true;
        }
    };

    /**
     * Built-in {@link AutoRepairResponder} implementation that forbids all auto-repairs.
     */
    public static final AutoRepairResponder AUTO_REPAIR_NONE = new AutoRepairResponder() {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean mayRepair(Finding repairableValidationFinding) {
            return false;
        }
    };

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8810465155456968453L;

    /**
     * The responder that determines whether the validator is to be allowed to auto-repair a finding. Default is the more
     * conservative value of allowing no auto-repairs.
     */
    private AutoRepairResponder autoRepairResponder = AUTO_REPAIR_NONE;

    /** The gedcom being validated. */
    private final Gedcom gedcom;

    /** The results. */
    private final ValidationResults results = new ValidationResults();

    /**
     * Instantiates a new validator.
     *
     * @param gedcom
     *            the gedcom being validated. Required.
     * @throws IllegalArgumentException
     *             if a null Gedcom is passed in.
     */
    public Validator(Gedcom gedcom) {
        if (gedcom == null) {
            throw new IllegalArgumentException("gedcom is a required argument");
        }
        this.gedcom = gedcom;
    }

    /**
     * Gets the auto repair responder.
     *
     * @return the auto repair responder
     */
    public AutoRepairResponder getAutoRepairResponder() {
        return autoRepairResponder;
    }

    /**
     * Get the gedcom
     * 
     * @return the gedcom
     */
    public Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * Get the results
     * 
     * @return the results
     */
    public ValidationResults getResults() {
        return results;
    }

    /**
     * Create a finding - automatically adds to the results.
     * 
     * @param itemOfConcern
     *            the item of concern. Required.
     * @param severity
     *            the severity. Required.
     * @param problemCode
     *            the problem code. Required.
     * @param fieldNameOfConcern
     *            the name of the field that has a problmatic value. Optional, but if supplied it needs to exist on the item of
     *            concern as a field
     * @throws IllegalArgumentException
     *             if any of the arguments are null
     * @return the finding just created and added to the results
     */
    public Finding newFinding(ModelElement itemOfConcern, Severity severity, ProblemCode problemCode, String fieldNameOfConcern) {
        if (itemOfConcern == null) {
            throw new IllegalArgumentException("itemOfConcern is a required argument.");
        }
        if (severity == null) {
            throw new IllegalArgumentException("severity is a required argument.");
        }
        if (problemCode == null) {
            throw new IllegalArgumentException("problemCode is a required argument.");
        }
        Finding f = new Finding();
        f.itemOfConcern = itemOfConcern;
        f.severity = severity;
        f.problemCode = problemCode.getCode();
        f.problemDescription = problemCode.getDescription();
        f.fieldNameOfConcern = fieldNameOfConcern;
        results.add(f);
        return f;
    }

    /**
     * Sets the auto repair responder.
     *
     * @param autoRepairResponder
     *            the new auto repair responder. Set to null if all auto-repair is to be disabled. You can also use
     */
    public void setAutoRepairResponder(AutoRepairResponder autoRepairResponder) {
        this.autoRepairResponder = autoRepairResponder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Validator [");
        if (results != null) {
            builder.append("results=");
            builder.append(results);
            builder.append(", ");
        }
        if (autoRepairResponder != null) {
            builder.append("autoRepairResponder=");
            builder.append(autoRepairResponder);
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Validate the gedcom
     */
    public void validate() {
        results.clear();
        new HeaderValidator(this, gedcom.getHeader()).validate();
        new SubmissionValidator(this, gedcom.getSubmission()).validate();
        if (gedcom.getTrailer() == null) {
            newFinding(gedcom, Severity.ERROR, ProblemCode.MISSING_REQUIRED_VALUE, "trailer");
        }
    }

    /**
     * Check if the finding can be auto-repaired. Delegates to the registered auto-repair responder, if any.
     * 
     * @param validationFinding
     *            the validation finding
     * @return true if the finding may be auto-repaired
     */
    boolean mayRepair(Finding validationFinding) {
        if (autoRepairResponder != null) {
            return autoRepairResponder.mayRepair(validationFinding);
        }
        return false;
    }

}
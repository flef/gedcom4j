package com.mattharrah.gedcom4j.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import com.mattharrah.gedcom4j.Address;
import com.mattharrah.gedcom4j.ChangeDate;
import com.mattharrah.gedcom4j.Citation;
import com.mattharrah.gedcom4j.Corporation;
import com.mattharrah.gedcom4j.EventRecorded;
import com.mattharrah.gedcom4j.Gedcom;
import com.mattharrah.gedcom4j.Header;
import com.mattharrah.gedcom4j.HeaderSourceData;
import com.mattharrah.gedcom4j.Multimedia;
import com.mattharrah.gedcom4j.Note;
import com.mattharrah.gedcom4j.Repository;
import com.mattharrah.gedcom4j.RepositoryCitation;
import com.mattharrah.gedcom4j.Source;
import com.mattharrah.gedcom4j.SourceCallNumber;
import com.mattharrah.gedcom4j.SourceData;
import com.mattharrah.gedcom4j.SourceSystem;
import com.mattharrah.gedcom4j.Submission;
import com.mattharrah.gedcom4j.Submitter;
import com.mattharrah.gedcom4j.UserReference;
import com.mattharrah.gedcom4j.validate.GedcomValidationException;
import com.mattharrah.gedcom4j.validate.GedcomValidator;

public class GedcomWriter {

	/**
	 * The Gedcom data to write
	 */
	private Gedcom gedcom;

	/**
	 * Are we suppressing the call to the validator? Deliberately
	 * package-private so unit tests can fiddle with it to make testing easy.
	 */
	boolean validationSuppressed = false;

	/**
	 * Constructor
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure to write out
	 */
	public GedcomWriter(Gedcom gedcom) {
		this.gedcom = gedcom;
	}

	/**
	 * Write the {@link Gedcom} data as a GEDCOM 5.5 file. Automatically fills
	 * in the value for the FILE tag in the HEAD structure.
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure to write
	 * @param file
	 *            the {@link File} to write to
	 * @throws IOException
	 *             if there's a problem writing the data
	 * @throws GedcomValidationException
	 *             if the gedcom data has validation errors that won't allow it
	 *             to be written
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	public void write(File file) throws IOException, GedcomValidationException,
	        GedcomWriterException {
		// Automatically replace the contents of the filename in the header
		gedcom.header.fileName = file.getName();

		OutputStream o = new FileOutputStream(file);
		write(o);
		o.flush();
		o.close();
	}

	/**
	 * Write the {@link Gedcom} data in GEDCOM 5.5 format to an output stream
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure to write
	 * @param out
	 *            the output stream we're writing to
	 * @throws GedcomValidationException
	 *             if the gedcom data has validation errors that won't allow it
	 *             to be written
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 * @throws FileNotFoundException
	 *             if the output file cannot be written (perhaps the directory
	 *             we're writing into doesn't exist?)
	 */
	public void write(OutputStream out) throws GedcomValidationException,
	        GedcomWriterException {
		if (!validationSuppressed) {
			GedcomValidator gv = new GedcomValidator(gedcom);
			gv.validate();
		}
		PrintWriter pw = new PrintWriter(out);
		emitHeader(pw);
		emitSubmissionRecord(pw);
		emitRecords(pw);
		emitTrailer(pw);
		pw.flush();
		pw.close();
	}

	/**
	 * Write the {@link Gedcom} data as a GEDCOM 5.5 file, with the supplied
	 * file name
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure to write
	 * @param filename
	 *            the name of the file to write
	 * @throws IOException
	 *             if there's a problem writing the data
	 * @throws GedcomValidationException
	 *             if the gedcom data has validation errors that won't allow it
	 *             to be written
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	public void write(String filename) throws IOException,
	        GedcomValidationException, GedcomWriterException {
		File f = new File(filename);
		write(f);
	}

	/**
	 * Write an address
	 * 
	 * @param pw
	 * @param level
	 * @param address
	 */
	private void emitAddress(PrintWriter pw, int level, Address address) {
		if (address == null) {
			return;
		}
		emitLinesOfText(pw, level, "ADDR", address.lines);
		emitTagIfValueNotNull(pw, level + 1, null, "ADR1", address.addr1);
		emitTagIfValueNotNull(pw, level + 1, null, "ADR2", address.addr2);
		emitTagIfValueNotNull(pw, level + 1, null, "CITY", address.city);
		emitTagIfValueNotNull(pw, level + 1, null, "STAE",
		        address.stateProvince);
		emitTagIfValueNotNull(pw, level + 1, null, "POST", address.postalCode);
		emitTagIfValueNotNull(pw, level + 1, null, "CTRY", address.country);
	}

	private void emitChangeDate(PrintWriter pw, int level, ChangeDate cd)
	        throws GedcomWriterException {
		if (cd != null) {
			emitTag(pw, level, null, "CHAN");
			emitTagWithRequiredValue(pw, level + 1, null, "DATE", cd.date);
			emitTagIfValueNotNull(pw, level + 2, null, "TIME", cd.time);
			emitNotes(pw, level + 1, cd.notes);
		}
	}

	/**
	 * Write out all the Families
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 */
	private void emitFamilies(PrintWriter pw) {
		// TODO write out families

	}

	/**
	 * Write the header record (see the HEADER structure in the GEDCOM standard)
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure containing the header
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitHeader(PrintWriter pw) throws GedcomWriterException {
		Header header = gedcom.header;
		if (header == null) {
			header = new Header();
		}
		pw.println("0 HEAD");
		emitSourceSystem(pw, header.sourceSystem);
		emitTagIfValueNotNull(pw, 1, null, "DEST", header.destinationSystem);
		if (header.date != null) {
			emitTagIfValueNotNull(pw, 1, null, "DATE", header.date);
			emitTagIfValueNotNull(pw, 2, null, "TIME", header.time);
		}
		if (header.submitter != null) {
			emitTagWithRequiredValue(pw, 1, null, "SUBM", header.submitter.xref);
		}
		if (header.submission != null) {
			emitTagWithRequiredValue(pw, 1, null, "SUBN",
			        header.submission.xref);
		}
		emitTagIfValueNotNull(pw, 1, null, "FILE", header.fileName);
		emitTagIfValueNotNull(pw, 1, null, "COPR", header.copyrightData);
		emitTag(pw, 1, null, "GEDC");
		emitTagWithRequiredValue(pw, 2, null, "VERS",
		        header.gedcomVersion.versionNumber);
		emitTagWithRequiredValue(pw, 2, null, "FORM",
		        header.gedcomVersion.gedcomForm);
		emitTagWithRequiredValue(pw, 1, null, "CHAR",
		        header.characterSet.characterSetName);
		emitTagIfValueNotNull(pw, 2, null, "VERS",
		        header.characterSet.versionNum);
		emitTagIfValueNotNull(pw, 1, null, "LANG", header.language);
		if (header.placeStructure != null && !header.placeStructure.isEmpty()) {
			// TODO - need better handling for PLAC structures in the header
			emitTag(pw, 1, null, "PLAC");
			emitTagWithRequiredValue(pw, 2, null, "FORM", header.placeStructure);
		}
		emitNote(pw, 1, header.notes);
	}

	/**
	 * Write out all the individuals
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 */
	private void emitIndividuals(PrintWriter pw) {
		// TODO write out individuals

	}

	/**
	 * Emit a multi-line text value.
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level we are starting at. Continuation lines will be one
	 *            level deeper than this value
	 * @param startingTag
	 *            the tag to use for the first line of the text. All subsequent
	 *            lines will be "CONT" lines.
	 * @param linesOfText
	 *            the lines of text
	 */
	private void emitLinesOfText(PrintWriter pw, int level, String startingTag,
	        List<String> linesOfText) {
		int lineNum = 0;
		for (String l : linesOfText) {
			if (lineNum++ == 0) {
				emitTagIfValueNotNull(pw, level, null, startingTag, l);
			} else {
				emitTagIfValueNotNull(pw, level + 1, null, "CONT", l);
			}
		}
	}

	/**
	 * Write out all the embedded multimedia objects
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitMultimedia(PrintWriter pw) throws GedcomWriterException {
		for (Multimedia m : gedcom.multimedia.values()) {
			emitTag(pw, 0, m.xref, "OBJE");
			emitTagWithRequiredValue(pw, 1, null, "FORM", m.format);
			emitTagIfValueNotNull(pw, 1, null, "TITL", m.title);
			emitNotes(pw, 1, m.notes);
			emitTag(pw, 1, null, "BLOB");
			for (String b : m.blob) {
				emitTagWithRequiredValue(pw, 2, null, "CONT", b);
			}
			if (m.continuedObject != null && m.continuedObject.xref != null) {
				emitTagWithRequiredValue(pw, 1, null, "OBJE",
				        m.continuedObject.xref);
			}
			for (UserReference u : m.userReferences) {
				emitTagWithRequiredValue(pw, 1, null, "REFN", u.referenceNum);
				emitTagIfValueNotNull(pw, 2, null, "TYPE", u.type);
			}
			emitTagIfValueNotNull(pw, 1, null, "RIN", m.recIdNumber);
			emitChangeDate(pw, 1, m.changeDate);

		}
	}

	/**
	 * Emit a list of multimedia links
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param multimedia
	 *            the {@link List} of {@link Multimedia} objects
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitMultimediaLinks(PrintWriter pw, int level,
	        List<Multimedia> multimedia) throws GedcomWriterException {
		if (multimedia == null) {
			return;
		}
		for (Multimedia m : multimedia) {
			if (m.xref != null) {
				// Link to the embedded form
				emitTagWithRequiredValue(pw, level, null, "OBJE", m.xref);
			} else {
				// Link to external file
				emitTag(pw, level, null, "OBJE");
				emitTagWithRequiredValue(pw, level + 1, null, "FORM", m.format);
				emitTagIfValueNotNull(pw, level + 1, null, "TITL", m.title);
				emitTagWithRequiredValue(pw, level + 1, null, "FILE",
				        m.fileReference);
				emitNotes(pw, level, m.notes);
			}
		}
	}

	/**
	 * Emit a note (possibly multi-line)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param notes
	 *            the Notes text
	 */
	private void emitNote(PrintWriter pw, int level, List<String> notes) {
		int noteLineNum = 0;
		for (String n : notes) {
			if (noteLineNum++ == 0) {
				emitTagIfValueNotNull(pw, level, null, "NOTE", n);
			} else {
				pw.println(level + 1 + " " + "CONT" + " "
				        + (n == null ? "" : n));
			}
		}
	}

	/**
	 * Emit a note structure (possibly multi-line)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param notes
	 *            the Notes text
	 */
	private void emitNote(PrintWriter pw, int level, Note note) {
		int noteLineNum = 0;
		for (String n : note.lines) {
			if (noteLineNum++ == 0) {
				emitTagIfValueNotNull(pw, level, null, "NOTE", n);
			} else {
				pw.println(level + 1 + " " + "CONT" + " "
				        + (n == null ? "" : n));
			}
		}
		emitSourceCitations(pw, level, note.citations);
	}

	/**
	 * Emit a list of note structures
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param notes
	 *            a list of {@link Note} structures
	 */
	private void emitNotes(PrintWriter pw, int level, List<Note> notes) {
		for (Note n : notes) {
			emitNote(pw, level, n);
		}
	}

	/**
	 * Write out all the notes
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 */

	private void emitNoteStructures(PrintWriter pw) {
		// TODO write out note structures

	}

	/**
	 * Write out a list of phone numbers
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param phoneNumbers
	 *            a list of phone numbers
	 */
	private void emitPhoneNumbers(PrintWriter pw, int level,
	        List<String> phoneNumbers) {
		for (String ph : phoneNumbers) {
			emitTagIfValueNotNull(pw, level, null, "PHON", ph);
		}
	}

	/**
	 * Write the records (see the RECORD structure in the GEDCOM standard)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitRecords(PrintWriter pw) throws GedcomWriterException {
		emitSubmitter(pw);
		emitIndividuals(pw);
		emitFamilies(pw);
		emitMultimedia(pw);
		emitNoteStructures(pw);
		emitRepositories(pw);
		emitSources(pw);
	}

	/**
	 * Write out all the repositories (see REPOSITORY_RECORD in the Gedcom spec)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data being written is malformed
	 */
	private void emitRepositories(PrintWriter pw) throws GedcomWriterException {
		for (Repository r : gedcom.repositories.values()) {
			emitTag(pw, 0, r.xref, "REPO");
			emitTagIfValueNotNull(pw, 1, null, "NAME", r.name);
			emitAddress(pw, 1, r.address);
			emitNotes(pw, 1, r.notes);
			for (UserReference u : r.userReferences) {
				emitTagWithRequiredValue(pw, 1, null, "REFN", u.referenceNum);
				emitTagIfValueNotNull(pw, 2, null, "TYPE", u.type);
			}
			emitTagIfValueNotNull(pw, 1, null, "RIN", r.recIdNumber);
			emitTagIfValueNotNull(pw, 1, null, "RFN", r.regFileNumber);
			emitPhoneNumbers(pw, 1, r.phoneNumbers);
			emitChangeDate(pw, 1, r.changeDate);
		}
	}

	/**
	 * Write out a repository citation (see SOURCE_REPOSITORY_CITATION in the
	 * gedcom spec)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level we're writing at
	 * @param repositoryCitation
	 *            the repository citation to write out
	 * @throws GedcomWriterException
	 *             if the repository citation passed in has a null repository
	 *             reference
	 */
	private void emitRepositoryCitation(PrintWriter pw, int level,
	        RepositoryCitation repositoryCitation) throws GedcomWriterException {
		if (repositoryCitation != null) {
			if (repositoryCitation.repository == null) {
				throw new GedcomWriterException(
				        "Repository Citation has null repository reference");
			}
			emitTagWithRequiredValue(pw, level, null, "REPO",
			        repositoryCitation.repository.xref);
			emitNotes(pw, level + 1, repositoryCitation.notes);
			for (SourceCallNumber scn : repositoryCitation.callNumbers) {
				emitTagWithRequiredValue(pw, level + 1, null, "CALN",
				        scn.callNumber);
				emitTagIfValueNotNull(pw, level + 2, null, "MEDI",
				        scn.mediaType);
			}
		}

	}

	/**
	 * Write out a list of source citations
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we are writing to
	 * @param level
	 *            the level in the hierarchy we are writing at
	 * @param citations
	 *            the source citations
	 */
	private void emitSourceCitations(PrintWriter pw, int level,
	        List<Citation> citations) {
		// TODO write out source citations

	}

	/**
	 * Write out all the sources (see SOURCE_RECORD in the Gedcom spec)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data being written is malformed
	 */
	private void emitSources(PrintWriter pw) throws GedcomWriterException {
		for (Source s : gedcom.sources.values()) {
			emitTag(pw, 0, s.xref, "SOUR");
			SourceData d = s.data;
			if (d != null) {
				emitTag(pw, 1, null, "DATA");
				for (EventRecorded e : d.eventsRecorded) {
					emitTagWithOptionalValue(pw, 2, null, "EVEN", e.eventType);
					emitTagIfValueNotNull(pw, 2, null, "DATE", e.datePeriod);
					emitTagIfValueNotNull(pw, 2, null, "PLAC", e.jurisdiction);
				}
				emitTagIfValueNotNull(pw, 2, null, "AGNC", d.respAgency);
				emitNotes(pw, 2, d.notes);
			}
			emitLinesOfText(pw, 1, "AUTH", s.originatorsAuthors);
			emitLinesOfText(pw, 1, "TITL", s.title);
			emitTagIfValueNotNull(pw, 1, null, "ABBR", s.sourceFiledBy);
			emitLinesOfText(pw, 1, "PUBL", s.publicationFacts);
			emitLinesOfText(pw, 1, "TEXT", s.sourceText);
			emitRepositoryCitation(pw, 1, s.repositoryCitation);
			emitMultimediaLinks(pw, 1, s.multimedia);
			emitNotes(pw, 1, s.notes);
			for (UserReference u : s.userReferences) {
				emitTagWithRequiredValue(pw, 1, null, "REFN", u.referenceNum);
				emitTagIfValueNotNull(pw, 2, null, "TYPE", u.type);
			}
			emitTagIfValueNotNull(pw, 1, null, "RIN", s.recIdNumber);
			emitTagIfValueNotNull(pw, 1, null, "RFN", s.regFileNumber);
			emitChangeDate(pw, 1, s.changeDate);
		}
	}

	/**
	 * Write a source system structure (see APPROVED_SYSTEM_ID in the GEDCOM
	 * spec)
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param sourceSystem
	 *            the source system
	 * @throws GedcomWriterException
	 *             if data is malformed and cannot be written
	 */
	private void emitSourceSystem(PrintWriter pw, SourceSystem sourceSystem)
	        throws GedcomWriterException {
		if (sourceSystem == null) {
			return;
		}
		emitTagIfValueNotNull(pw, 1, null, "SOUR", sourceSystem.systemId);
		emitTagWithOptionalValue(pw, 2, null, "VERS", sourceSystem.versionNum);
		emitTagWithOptionalValue(pw, 2, null, "NAME", sourceSystem.productName);
		Corporation corporation = sourceSystem.corporation;
		if (corporation != null) {
			emitTagWithOptionalValue(pw, 2, null, "CORP",
			        corporation.businessName);
			emitAddress(pw, 3, corporation.address);
			emitPhoneNumbers(pw, 3, corporation.phoneNumbers);
		}
		HeaderSourceData sourceData = sourceSystem.sourceData;
		if (sourceData != null) {
			emitTagIfValueNotNull(pw, 2, null, "DATA", sourceData.name);
			emitTagIfValueNotNull(pw, 3, null, "DATE", sourceData.publishDate);
			emitTagIfValueNotNull(pw, 3, null, "COPR", sourceData.copyright);
		}
	}

	/**
	 * Write the SUBMISSION_RECORD
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure containing the header
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitSubmissionRecord(PrintWriter pw)
	        throws GedcomWriterException {
		Submission s = gedcom.submission;
		if (s == null) {
			return;
		}
		emitTag(pw, 0, s.xref, "SUBN");
		if (s.submitter != null) {
			emitTagWithOptionalValue(pw, 1, null, "SUBM", s.submitter.xref);
		}
		emitTagIfValueNotNull(pw, 1, null, "FAMF", s.nameOfFamilyFile);
		emitTagIfValueNotNull(pw, 1, null, "TEMP", s.templeCode);
		emitTagIfValueNotNull(pw, 1, null, "ANCE", s.ancestorsCount);
		emitTagIfValueNotNull(pw, 1, null, "DESC", s.descendantsCount);
		emitTagIfValueNotNull(pw, 1, null, "ORDI", s.ordinanceProcessFlag);
		emitTagIfValueNotNull(pw, 1, null, "RIN", s.recIdNumber);
	}

	/**
	 * Write out the submitter record
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @throws GedcomWriterException
	 *             if the data is malformed and cannot be written
	 */
	private void emitSubmitter(PrintWriter pw) throws GedcomWriterException {
		for (Submitter s : gedcom.submitters.values()) {
			emitTag(pw, 0, s.xref, "SUBM");
			emitTagWithOptionalValue(pw, 1, null, "NAME", s.name);
			emitAddress(pw, 1, s.address);
			emitMultimediaLinks(pw, 1, s.multimedia);
			for (String l : s.languagePref) {
				emitTagWithRequiredValue(pw, 1, null, "LANG", l);
			}
			/*
			 * Unclear if really part of the GEDCOM or not - a stress test file
			 * includes them, and the tool can parse them, so if we have them,
			 * write them. Won't hurt anything if the collection is empty.
			 */
			for (String l : s.phoneNumbers) {
				emitTagWithRequiredValue(pw, 1, null, "PHON", l);
			}

			emitTagIfValueNotNull(pw, 1, null, "RFN", s.regFileNumber);
			emitTagIfValueNotNull(pw, 1, null, "RIN", s.recIdNumber);
			emitChangeDate(pw, 1, s.changeDate);
		}
	}

	/**
	 * Write a line with a tag, with no value following the tag
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level within the file hierarchy
	 * @param tag
	 *            the tag for the line of the file
	 */
	private void emitTag(PrintWriter pw, int level, String xref, String tag) {
		pw.print(level);
		if (xref != null && !xref.isEmpty()) {
			pw.print(" " + xref);
		}
		pw.println(" " + tag);
	}

	/**
	 * Write a line if the value is non-null
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level within the file hierarchy
	 * @param tag
	 *            the tag for the line of the file
	 * @param value
	 *            the value to write to the right of the tag
	 */
	private void emitTagIfValueNotNull(PrintWriter pw, int level, String xref,
	        String tag, String value) {
		if (value != null) {
			pw.print(level);
			if (xref != null && !xref.isEmpty()) {
				pw.print(" " + xref);
			}
			pw.println(" " + tag + " " + value);
		}
	}

	/**
	 * Write a line and tag, with an optional value for the tag
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level within the file hierarchy
	 * @param xref
	 *            the xref ID identifying this entry
	 * @param tag
	 *            the tag for the line of the file
	 * @param value
	 *            the value to write to the right of the tag
	 * @throws GedcomWriterException
	 *             if the value is null or blank (which never happens, because
	 *             we check for it)
	 */
	private void emitTagWithOptionalValue(PrintWriter pw, int level,
	        String xref, String tag, String value) throws GedcomWriterException {
		if (value == null) {
			emitTag(pw, level, xref, tag);
		} else {
			pw.print(level);
			if (xref != null && !xref.isEmpty()) {
				pw.print(" " + xref);
			}
			pw.println(" " + tag + " " + value);
		}
	}

	/**
	 * Write a line and tag, with an optional value for the tag
	 * 
	 * @param pw
	 *            the {@link PrintWriter} we're writing to
	 * @param level
	 *            the level within the file hierarchy
	 * @param tag
	 *            the tag for the line of the file
	 * @param value
	 *            the value to write to the right of the tag
	 * @param xref
	 * @throws GedcomWriterException
	 *             if the value is null or blank
	 */
	private void emitTagWithRequiredValue(PrintWriter pw, int level,
	        String xref, String tag, String value) throws GedcomWriterException {
		if (value == null || "".equals(value)) {
			throw new GedcomWriterException("Required value for tag " + tag
			        + " at level " + level + " was null or blank");
		}
		pw.print(level);
		if (xref != null && !xref.isEmpty()) {
			pw.print(" " + xref);
		}
		pw.println(" " + tag + " " + value);
	}

	/**
	 * Write out the trailer record
	 * 
	 * @param gedcom
	 *            the {@link Gedcom} structure containing the header
	 */
	private void emitTrailer(PrintWriter pw) {
		pw.println("0 TRLR");
	}
}
